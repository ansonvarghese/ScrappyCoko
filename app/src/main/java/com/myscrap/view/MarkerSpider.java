package com.myscrap.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import com.androidmapsextensions.ClusterGroup;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.Polyline;
import com.androidmapsextensions.PolylineOptions;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.myscrap.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MarkerSpider {

    private static final double TWO_PI = Math.PI * 2;
    private static final double RADIUS_SCALE_FACTOR = 10;
    private static final double SPIRAL_ANGLE_STEP = 0.2; //in radians
    private static final String ARG_KEEP_SPIDER_FIED = "keepSpiderfied";
    private static final String ARG_MARK_WONT_HIDE  = "markersWontHide";
    private static final String ARG_MARK_WONT_MOVE  = "markersWontMove";
    private static final String ARG_NEARBY_DISTANCE = "nearbyDistance";
    private static final String ARG_CS_SWITCHOVER   = "circleSpiralSwitchover";
    private static final String ARG_LEG_WEIGHT      = "legWeight";
    private GoogleMap googleMap;
    private Context mContext;
    private Projection projection;



    private class _omsData{
        private LatLng usualPosition;
        private Polyline leg;

        LatLng getUsualPosition() {
            return usualPosition;
        }

        public Polyline getLeg() {
            return leg;
        }

        _omsData leg(Polyline newLeg){
            if(leg!=null)
                leg.remove();
            leg=newLeg;
            return this; // return self, for chaining
        }

        _omsData usualPosition(LatLng newUsualPos){
            usualPosition=newUsualPos;
            return this; // return self, for chaining
        }

    }

    private class MarkerData{
        Marker marker;
        Point markerPt;
        MarkerData(Marker mark, Point pt){
            marker = mark;
            markerPt = pt;
        }
    }

    private class LegColor{

        private final int type_satellite;
        private final int type_normal; // in the javascript version this is known as "roadmap"

        LegColor(int set, int road){
            type_satellite = set;
            type_normal = road;
        }

        public int getType_satellite() {
            return type_satellite;
        }

        public int getType_normal() {
            return type_normal;
        }
    }

    public final LegColor usual       = new LegColor(0xAAFFFFFF,0xAA0F0F0F);


    //the following lists are initialized later
    private List<Marker> markersInCluster; // refers to the current clicked cluster
    private List<Marker> spiderfiedClusters; // as the name suggests
    private List<Marker> spiderfiedUnclusteredMarkers; // intended to hold makers that were tightly packed but not clustered before spiderfying

    private boolean spiderfying = false;   //needed for recursive spiderfication
    private boolean isAnythingSpiderfied = false;
    private float zoomLevelOnLastSpiderfy;

    private HashMap<Marker,_omsData> omsData= new HashMap<Marker, _omsData>();
    private HashMap<Marker,Boolean> spiderfyable = new HashMap<Marker, Boolean>();


    public MarkerSpider(GoogleMap googleMap, Object... varArgs) throws IllegalArgumentException {
        this.googleMap = googleMap;
        if (varArgs.length > 0)
            assignVarArgs(varArgs);
        initMarkerArrays();

        // Listeners:
        googleMap.setOnCameraChangeListener(cameraPosition -> {
            if(spiderfiedClusters.size()>0 && cameraPosition.zoom != zoomLevelOnLastSpiderfy)
                unspiderfyAll();
        });
    }
    public MarkerSpider(Context context, GoogleMap googleMap) throws IllegalArgumentException {
        this.mContext = context;
        this.googleMap = googleMap;
        initMarkerArrays();

        // Listeners:
        googleMap.setOnCameraChangeListener(cameraPosition -> {
            if(spiderfiedClusters.size()>0 && cameraPosition.zoom != zoomLevelOnLastSpiderfy)
                unspiderfyAll();
        });
    }

    private void initMarkerArrays(){
        markersInCluster = new ArrayList<>();
        spiderfiedClusters = new ArrayList<>();
        spiderfiedUnclusteredMarkers = new ArrayList<>();
    }

    private List<Point> generatePtsCircle (int count, Point centerPt){
        int circleFootSeparation = 23;
        int circumference = circleFootSeparation * ( 2 + count);
        double legLength = circumference / TWO_PI * RADIUS_SCALE_FACTOR; // = radius from circumference
        double angleStep = TWO_PI / count;
        double angle;
        List<Point> points = new ArrayList<Point>(count);
        for (int ind = 0; ind < count; ind++) {
            double circleStartAngle = TWO_PI / 12;
            angle = circleStartAngle + ind * angleStep;
            points.add(new Point((int)(centerPt.x + legLength * Math.cos(angle)),(int)(centerPt.y + legLength * Math.sin(angle))));
        }
        return points;
    }

    private List<Point> generatePtsSpiral (int count, Point centerPt){
        int spiralLengthStart = 11;
        double legLength = spiralLengthStart * RADIUS_SCALE_FACTOR;
        double angle = 0;
        List<Point> points = new ArrayList<Point>(count);
        for (int ind = 0; ind < count; ind++) {
            int spiralFootSeparation = 26;
            angle += spiralFootSeparation / legLength + ind * SPIRAL_ANGLE_STEP;
            points.add(new Point((int)(centerPt.x + legLength * Math.cos(angle)),(int)(centerPt.y + legLength * Math.sin(angle))));
            int spiralLengthFactor = 4;
            legLength += TWO_PI * spiralLengthFactor / angle;
        }
        return points;
    }

    public void spiderListener(Marker cluster){ /** Corresponds to line 138 of original code*/

        if (isAnythingSpiderfied && !spiderfying){ // unspiderfy everything before spiderfying anything new
            unspiderfyAll();
        }
        List<MarkerData> closeMarkers = new ArrayList<MarkerData>();
        List<MarkerData> displayedFarMarkers   = new ArrayList<MarkerData>();
        int nDist = 20;
        int pxSq = nDist * nDist;
        Point mPt, markerPt = llToPt(cluster.getPosition());
        List<Marker> tmpMarkersInCluster = new ArrayList<Marker>();
        tmpMarkersInCluster.addAll(cluster.getMarkers());
        markersInCluster.addAll(cluster.getMarkers());


        for (Marker markers_item : tmpMarkersInCluster) {
            if (markers_item.isCluster()) {
                recursiveAddMarkersToSpiderfy(markers_item);
            }
            mPt = projection.toScreenLocation(markers_item.getPosition());
            if (ptDistanceSq(mPt,markerPt) < pxSq)
                closeMarkers.add(new MarkerData(markers_item,mPt));
            else
                displayedFarMarkers.add(new MarkerData(markers_item,mPt));
        }

        spiderfy(closeMarkers,displayedFarMarkers);
        spiderfiedClusters.add(cluster);
        zoomLevelOnLastSpiderfy = googleMap.getCameraPosition().zoom;
    }

    private void recursiveAddMarkersToSpiderfy(Marker markers_item) {
        List<Marker> nestedMarkers = markers_item.getMarkers();
        for (Marker nestedMarker : nestedMarkers) {
            if (!nestedMarker.isCluster()) // inception.... (cluster within a cluster within a cl.....)
                tryAddMarker(markersInCluster,nestedMarker);
            else
                recursiveAddMarkersToSpiderfy(markers_item);
        }
    }


    private void spiderfy(List<MarkerData> clusteredMarkersData,List<MarkerData> nearbyMarkers){
        List<MarkerData> listToUse = new ArrayList<>();
        listToUse.addAll(clusteredMarkersData); listToUse.addAll(nearbyMarkers); //could be terrible... :P
        spiderfying = true;
        int numFeet = listToUse.size();
        List<Point> nearbyMarkerPts = new ArrayList<>(numFeet);
        for (MarkerData markerData : listToUse) {
            nearbyMarkerPts.add(markerData.markerPt);
        }
        Point bodyPt = ptAverage(nearbyMarkerPts);
        List<Point> footPts;
        int circleSpiralSwitchover = 9;
        if (numFeet >= circleSpiralSwitchover){
            footPts=generatePtsSpiral(numFeet,bodyPt);
            Collections.reverse(footPts);
        }
        else
            footPts=generatePtsCircle(numFeet,bodyPt);

        for (int ind =0; ind < numFeet; ind++){
            Point footPt = footPts.get(ind);
            LatLng footLl = ptToLl(footPt);
            MarkerData nearestMarkerData = listToUse.get(ind);
            Marker clusterNearestMarker = nearestMarkerData.marker;
            float legWeight = 3F;
            int usualLegZIndex = 0;
            Polyline leg = googleMap.addPolyline(new PolylineOptions()
                    .add(clusterNearestMarker.getPosition(), footLl)
                    .color(usual.getType_normal())
                    .width(legWeight)
                    .zIndex(usualLegZIndex));
            omsData.put(clusterNearestMarker,new _omsData()
                    .leg(leg)
                    .usualPosition(clusterNearestMarker.getPosition()));
            clusterNearestMarker.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
            clusterNearestMarker.animatePosition(footLl);
            clusterNearestMarker.setIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.myscrap_pin_final)));
            spiderfiedUnclusteredMarkers.add(clusterNearestMarker);
        }
        isAnythingSpiderfied=true;
        spiderfying=false;
    }

    private Marker unspiderfy(Marker markerToUnspiderfy){ //241
        // this function has to return everything to its original state
        if (markerToUnspiderfy!=null){
            boolean unspiderfying = true;
            if(markerToUnspiderfy.isCluster()){
                List<Marker> unspiderfiedMarkers = new ArrayList<>(), nonNearbyMarkers = new ArrayList<>();
                for (Marker marker : markersInCluster) {
                    if(omsData.containsKey(marker)){
                        marker.setPosition(omsData.get(marker).leg(null).getUsualPosition());
                        marker.setClusterGroup(ClusterGroup.DEFAULT);
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.myscrap_pin_final)));
                        unspiderfiedMarkers.add(marker);
                    } else
                        nonNearbyMarkers.add(marker);
                }
            } else {
                // if a regular (non-cluster) marker
                markerToUnspiderfy.setPosition(omsData.get(markerToUnspiderfy).leg(null).getUsualPosition());
                markerToUnspiderfy.setClusterGroup(ClusterGroup.DEFAULT);
                markerToUnspiderfy.setIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.myscrap_pin_final)));
            }
            unspiderfying =false;
        }
        return markerToUnspiderfy; // return self, for chaining
    }

    private int ptDistanceSq(Point pt1, Point pt2){ /** Corresponds to line 264 of original code*/
        int dx = pt1.x - pt2.x;
        int dy = pt1.y - pt2.y;
        return (dx * dx + dy * dy);
    }

    private Point ptAverage(List<Point> pts){ /** Corresponds to line 269 of original code*/
        int sumX=0, sumY=0, numPts=pts.size();
        for (Point pt : pts) {
            sumX += pt.x;
            sumY += pt.y;
        }
        return new Point(sumX / numPts,sumY / numPts);
    }

    private Point llToPt(LatLng ll) { /** Corresponds to line 276 of original code*/
        projection = googleMap.getProjection();
        return projection.toScreenLocation(ll);   // the android maps api equivalent
    }

    private LatLng ptToLl(Point pt){ /** Corresponds to line 277 of original code*/
        projection = googleMap.getProjection();
        return projection.fromScreenLocation(pt); // the android maps api equivalent
    }

    private boolean assignVarArgs(Object[] varArgs){
        int varLen=varArgs.length;
        if(varLen % 2 != 0){
            throw new IllegalArgumentException("Number of args is uneven.");
        }
        for(int ind=0; ind<varLen; ind=+2){
            String key = (String) varArgs[ind];
            if(key.equals(ARG_KEEP_SPIDER_FIED)){}
            else if(key.equals(ARG_MARK_WONT_HIDE)){}
            else if(key.equals(ARG_MARK_WONT_MOVE)){}
            else if(key.equals(ARG_NEARBY_DISTANCE)){}
            else if(key.equals(ARG_CS_SWITCHOVER)){}
            else if(key.equals(ARG_LEG_WEIGHT)){}
            else throw new IllegalArgumentException("Invalid argument name.");
        }
        return true;
    }

    private void unspiderfyAll() {
        for (Marker lastSpiderfiedCluster : spiderfiedClusters) {
            unspiderfy(lastSpiderfiedCluster);
        }
        for (Marker marker : spiderfiedUnclusteredMarkers) {
            unspiderfy(marker);
        }
        initMarkerArrays(); //hopefully, return to the initial state
        isAnythingSpiderfied=false;
    }

    private boolean tryAddMarker(Collection<Marker> collection, Marker obj){
        if (collection.contains(obj))
            return false;
        else {
            collection.add(obj);
            return true;
        }

    }
}