import React, { useEffect, useRef } from 'react';
import { UIManager, findNodeHandle, PixelRatio } from 'react-native';

import { PoiMapViewManager } from './PoiMapViewManager';

const createFragment = (viewId) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'create' command
    UIManager.PoiMapViewManager.Commands.create.toString(),
    [viewId]
  );

export const PoiMapView = (props) => {
  const ref = useRef(null);

  useEffect(() => {
    const viewId = findNodeHandle(ref.current);
    createFragment(viewId);
  }, []);

  return (
    <PoiMapViewManager
    language = {props.language}
    showPointOnMap = {props.showPointOnMap}
    getRouteTo = {props.getRouteTo}
      style={{
        flex: 1
      }}
      ref={ref}
    />
  );
};