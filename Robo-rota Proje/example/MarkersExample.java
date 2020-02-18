/*
 * Copyright (c) 2000-2017 TeamDev Ltd. All rights reserved.
 * Use is subject to Apache 2.0 license terms.
 */
//package com.teamdev.jxmaps.examples;

import com.teamdev.jxmaps.ControlPosition;
import com.teamdev.jxmaps.InfoWindow;
import com.teamdev.jxmaps.LatLng;
import com.teamdev.jxmaps.Map;
import com.teamdev.jxmaps.MapMouseEvent;
import com.teamdev.jxmaps.MapOptions;
import com.teamdev.jxmaps.MapReadyHandler;
import com.teamdev.jxmaps.MapStatus;
import com.teamdev.jxmaps.MapTypeControlOptions;
import com.teamdev.jxmaps.Marker;
import com.teamdev.jxmaps.MouseEvent;
import com.teamdev.jxmaps.swing.MapView;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * This example demonstrates how to create and customize a Marker on the Map.
 *
 * @author Vitaly Eremenko
 */
public class MarkersExample extends MapView {
	public static Map map;
	public static Marker marker;
	public MarkersExample() {
        setOnMapReadyHandler(new MapReadyHandler() {
            @Override
            public void onMapReady(MapStatus status) {
                if (status == MapStatus.MAP_STATUS_OK) {
                    map=getMap();
                	MapOptions options = new MapOptions();
                    MapTypeControlOptions controlOptions = new MapTypeControlOptions();
                    controlOptions.setPosition(ControlPosition.TOP_RIGHT);
                    options.setMapTypeControlOptions(controlOptions);
                    marker= new Marker(map);
                    map.setOptions(options);
                    map.setCenter(new LatLng(38.418897, 27.128678));
                    map.setZoom(9.0);
                    map.addEventListener("click", new MapMouseEvent() {
                        @Override
                        public void onEvent(MouseEvent mouseEvent) {
                        	marker.setVisible(true);
                            marker.setPosition(mouseEvent.latLng());
                            App.Lat.setText(Double.toString(mouseEvent.latLng().getLat()));
                            App.Long.setText(Double.toString(mouseEvent.latLng().getLng()));
                            App.Mrk=marker;
                        }
                    });
                }
            }
        });
    }

    public static void main(String[] args) {
        final MarkersExample sample = new MarkersExample();

        /*JFrame frame = new JFrame("Markers");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(sample, BorderLayout.CENTER);
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);*/
    }
}