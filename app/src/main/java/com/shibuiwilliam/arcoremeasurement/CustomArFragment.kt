package com.shibuiwilliam.arcoremeasurement

import com.google.ar.core.CameraConfig
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment

class CustomArFragment : ArFragment() {

    override fun getSessionConfiguration(session: Session?): Config {
        var config = Config(session)
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        config.focusMode = Config.FocusMode.AUTO
        //config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        //config.imageStabilizationMode = Config.ImageStabilizationMode.EIS
        //config.streetscapeGeometryMode = Config.StreetscapeGeometryMode.ENABLED
        //config.geospatialMode = Config.GeospatialMode.ENABLED
        session?.configure(config)

        this.arSceneView.setupSession(session)
        return config
    }
}