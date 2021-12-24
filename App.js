/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React from 'react';
import { useEffect, useRef } from 'react';
import type {Node} from 'react';
import {
  StyleSheet,
  useColorScheme,
  View,
  NativeModules,
  Button,
} from 'react-native';

import MapView from './MapView.js';
import { PoiMapView } from './PoiMapView.js';
import {
  Colors
} from 'react-native/Libraries/NewAppScreen';

const { PoiMapModule } = NativeModules;

const App: () => Node = () => {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };



  if(Platform.OS === 'ios') {
    return (
      <View style={{flex: 1}}>
          <View style={{height: 100, backgroundColor:"blue"}}/>
          <MapView language={"en"} getRouteTo={"store_id"} showOnMap={"store_id"} style={{ flex: 1}}/>
          <View style={{height: 100, backgroundColor:"blue"}}>
          <Button
            title="Show pin on map"
            onPress={() => {
              NativeModules.PoilabsNavigationBridge.showPointOnMap("store_id");
            }
            }
          />
            <Button
            title="Get route"
            onPress={() => {
              NativeModules.PoilabsNavigationBridge.getRouteTo("store_id");
            }
            }
          />
          </View>
      </View>
    );
  } else {
    return (
      <View style={{flex: 1}}>
          <View style={{height: 100, backgroundColor:"blue"}}/>
          <PoiMapView/>
          <View style={{height: 100, backgroundColor:"red"}}>
          <Button
            title="Show pin on map"
            onPress={() => {
              NativeModules.PoiMapModule.showPointOnMap(["2012001"]);
            }
            }
          />
            <Button
            title="Get route"
            onPress={() => {
              NativeModules.PoiMapModule.getRouteTo("2012001");
            }
            }
          />
          </View>
      </View>
    );
  }



};

const styles = StyleSheet.create({
  container: {
    marginTop: 10,
    padding: 10,
    color: '#ff0000',
    justifyContent: 'space-around',
    flex:1
  },
});

export default App;
