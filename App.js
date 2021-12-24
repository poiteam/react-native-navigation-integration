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
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
  NativeModules,
  Button,
  PixelRatio
} from 'react-native';

import MapView from './MapView.js';
import {
  Colors
} from 'react-native/Libraries/NewAppScreen';

import { UIManager, findNodeHandle } from 'react-native';


import { requireNativeComponent } from 'react-native';

export const PoiMapViewManager = requireNativeComponent(
  'PoiMapViewManager'
);
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
    const createFragment = (viewId) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'create' command
    UIManager.PoiMapViewManager.Commands.create.toString(),
    [viewId]
  );

  const ref = useRef(null);

  useEffect(() => {
    const viewId = findNodeHandle(ref.current);
    createFragment(viewId);
  }, []);
    return (
      <View style={{flex: 1}}>
          <View style={{height: 100, backgroundColor:"blue"}}/>
          <PoiMapViewManager
          showPointOnMap = "store_id"
          language = "en"
          style={{flex: 1}}
          ref={ref}
      />
          <View style={{height: 100, backgroundColor:"red"}}>
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
