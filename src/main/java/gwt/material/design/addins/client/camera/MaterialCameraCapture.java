package gwt.material.design.addins.client.camera;

/*
 * #%L
 * GwtMaterial
 * %%
 * Copyright (C) 2015 - 2017 GwtMaterialDesign
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.event.shared.HandlerRegistration;
import gwt.material.design.addins.client.camera.base.HasCameraCaptureHandlers;
import gwt.material.design.addins.client.camera.events.CameraCaptureEvent;
import gwt.material.design.addins.client.camera.events.CameraCaptureEvent.CaptureStatus;
import gwt.material.design.addins.client.camera.events.CameraCaptureHandler;
import gwt.material.design.client.base.MaterialWidget;
import gwt.material.design.jscore.client.api.*;

import static gwt.material.design.addins.client.camera.JsCamera.$;


//@formatter:off

/**
 * <p>
 * MaterialCameraCapture is a widget that captures the video stream from the camera, using the
 * HTML5 {@code MediaDevices.getUserMedia()} (Streams API). This widget can capture images from the video, to allow
 * the upload to the server of photos from the camera.
 * </p>
 * <p>
 * <h3>XML Namespace Declaration</h3>
 * <pre>
 * {@code
 * xmlns:ma='urn:import:gwt.material.design.addins.client'
 * }
 * </pre>
 * <p>
 * <h3>UiBinder Usage:</h3>
 * <p><pre>
 * {@code
 * <ma:camera.MaterialCameraCapture ui:field="camera" />
 * }
 * </pre></p>
 * <p>
 * <h3>Java Usage:</h3>
 * <p><pre>
 *  if (MaterialCameraCapture.isSupported()){
 *      MaterialCameraCapture camera = new MaterialCameraCapture();
 *      camera.addCameraCaptureHandler(new CameraCaptureHandler() {
 *          {@literal @}Override
 *          public void onCameraCaptureChange(CameraCaptureEvent event) {
 *              if (event.getCaptureStatus() == CaptureStatus.ERRORED){
 *                  Window.alert("Error on starting the camera capture: " + event.getErrorMessage());
 *                  ((MaterialCameraCapture)event.getSource()).removeFromParent();
 *              }
 *          }
 *      });
 *      add(camera); //adds to the layout
 *  }
 *  else {
 *      Window.alert("Sorry, your browser doesn't support the camera capture.");
 *  }
 * </pre></p>
 * <p>
 * <h3>Styling:</h3>
 * <p>
 * To limit the width of the camera capture widget on mobile devices, you can use {@code max-width: 100%} on the widget.
 * The browser will take care of the aspect ratio of the video.
 * </p>
 * <p>
 * <h3>Notice:</h3>
 * <p>
 * This widget only works on pages served by a secure protocol (HTTPS). For the browser compatibility,
 * access <a href="http://caniuse.com/#feat=stream">http://caniuse.com/#feat=stream</a>
 * </p>
 *
 * @author gilberto-torrezan
 */
// @formatter:on
public class MaterialCameraCapture extends MaterialWidget implements HasCameraCaptureHandlers {

    protected boolean pauseOnUnload = true;

    public MaterialCameraCapture() {
        super(Document.get().createVideoElement());
    }

    /**
     * Captures the current frame of the video to an image data URL. It's the same as calling
     * {@link #captureToDataURL(String)} using "image/png".
     *
     * @return The Data URL of the captured image, which can be used as "src" on an "img" element
     * or sent to the server
     */
    public String captureToDataURL() {
        return captureToDataURL("image/png");
    }

    /**
     * Captures the current frame of the video to an image data URL.
     *
     * @param mimeType The type of the output image, such as "image/png" or "image/jpeg".
     * @return The Data URL of the captured image, which can be used as "src" on an "img" element
     * or sent to the server
     */
    public String captureToDataURL(String mimeType) {
        return nativeCaptureToDataURL(Canvas.createIfSupported().getCanvasElement(), getElement(), mimeType);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        VideoElement el = getElement().cast();
        if (el.getSrc() == null || el.isPaused()) {
            play();
        }
    }

    @Override
    protected void onUnload() {
        if (pauseOnUnload) {
            pause();
        }
    }

    /**
     * <p>
     * Starts the video stream from the camera. This is called when the component is loaded.
     * Use {@link CameraCaptureHandler}s to be notified when the stream actually starts or if
     * an error occurs.
     * </p>
     * <p>
     * At this point the user is requested by the browser to allow the application to use the camera.
     * If the user doesn't allow it, an error is notified to the {@link CameraCaptureHandler}s.
     * </p>
     */
    public void play() {
        if (!isSupported()) {
            onCameraCaptureError("MaterialCameraCapture is not supported in this browser.");
            return;
        }
        VideoElement el = getElement().cast();
        if (el.getSrc() == null) {
            nativePlay(getElement());
        } else {
            el.play();
        }
    }

    /**
     * Restarts the video stream from the camera.
     * The user is requested again by the browser to allow the application to use the camera.
     */
    public void restart() {
        if (!isSupported()) {
            onCameraCaptureError("MaterialCameraCapture is not supported in this browser.");
            return;
        }
        nativePlay(getElement());
    }

    /**
     * Pauses the video stream from the camera.
     */
    public void pause() {
        VideoElement el = getElement().cast();
        el.pause();
        onCameraCapturePause();
    }

    /**
     * Sets if the camera capture should pause when the widget is unloaded.
     * The default is <code>true</code>.
     */
    public void setPauseOnUnload(boolean pauseOnUnload) {
        this.pauseOnUnload = pauseOnUnload;
    }

    /**
     * Returns if the camera capture should pause when the widget is unloaded.
     * The default is <code>true</code>.
     */
    public boolean isPauseOnUnload() {
        return pauseOnUnload;
    }

    /**
     * Native call to the streams API
     */
    protected void nativePlay(Element video) {
        MediaStream stream = null;
        if (Navigator.getUserMedia != null) {
            stream = Navigator.getUserMedia;
            GWT.log("Uses Default user Media");
        } else if (Navigator.webkitGetUserMedia != null) {
            stream = Navigator.webkitGetUserMedia;
            GWT.log("Uses Webkit User Media");
        } else if (Navigator.mozGetUserMedia != null) {
            stream = Navigator.mozGetUserMedia;
            GWT.log("Uses Moz User Media");
        } else if (Navigator.msGetUserMedia != null) {
            stream = Navigator.msGetUserMedia;
            GWT.log("Uses Microsoft user Media");
        } else {
            GWT.log("No supported media found in your browser");
        }

        if (stream != null) {
            Navigator.getMedia = stream;
            Constraints constraints = new Constraints();
            constraints.video = true;
            constraints.audio = false;

            Navigator.getMedia(constraints, (streamObj) -> {
                if (URL.createObjectURL(streamObj) != null) {
                    $(video).attr("src", URL.createObjectURL(streamObj));
                } else if (WebkitURL.createObjectURL(streamObj) != null) {
                    $(video).attr("src", WebkitURL.createObjectURL(streamObj));
                }
                if (video instanceof VideoElement) {
                    ((VideoElement) video).play();
                }
                onCameraCaptureLoad();
            }, (error) -> {
                GWT.log("MaterialCameraCapture: An error occured! " + error);
                onCameraCaptureError(error);
            });
        }
    }

    /**
     * Native call to capture the frame of the video stream.
     */
    protected String nativeCaptureToDataURL(CanvasElement canvas, Element element, String mimeType) {
        VideoElement videoElement = (VideoElement) element;
        int width = videoElement.getVideoWidth();
        int height = videoElement.getVideoHeight();
        if (Double.isNaN(width) || Double.isNaN(height)) {
            width = videoElement.getClientWidth();
            height = videoElement.getClientHeight();
        }
        canvas.setWidth(width);
        canvas.setHeight(height);
        Context2d context = canvas.getContext2d();
        context.drawImage(videoElement, 0, 0, width, height);
        return canvas.toDataUrl(mimeType);
    }

    /**
     * Tests if the browser supports the Streams API. This should be called before creating any
     * MaterialCameraCapture widgets to avoid errors on the browser.
     *
     * @return <code>true</code> if the browser supports this widget, <code>false</code> otherwise
     */
    public static boolean isSupported() {
        return Navigator.webkitGetUserMedia != null
            || Navigator.getUserMedia != null
            || Navigator.mozGetUserMedia != null
            || Navigator.msGetUserMedia != null;
    }

    /**
     * Called by the component when the stream has started.
     */
    protected void onCameraCaptureLoad() {
        CameraCaptureEvent.fire(this, CaptureStatus.STARTED);
    }

    /**
     * Called  by the component when the stream has paused.
     */
    protected void onCameraCapturePause() {
        CameraCaptureEvent.fire(this, CaptureStatus.PAUSED);
    }

    /**
     * Called by the component when the stream when an error occurs.
     */
    protected void onCameraCaptureError(String error) {
        CameraCaptureEvent.fire(this, error);
    }

    @Override
    public HandlerRegistration addCameraCaptureHandler(final CameraCaptureHandler handler) {
        return addHandler(event -> {
            if (isEnabled()) {
                handler.onCameraCaptureChange(event);
            }
        }, CameraCaptureEvent.getType());
    }

}
