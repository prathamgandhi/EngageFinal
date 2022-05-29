package xyz.prathamgandhi.copx;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.google.mlkit.vision.face.FaceDetector;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;

import java.util.List;

class BoundingBoxFrameAnalyzer implements ImageAnalysis.Analyzer {

    FaceDetector faceDetector;
    DrawBoundingBox drawBoundingBox;
    boolean Orientation;

    public static final boolean FRONT = true;
    public static final boolean BACK = false;

    BoundingBoxFrameAnalyzer(FaceDetector faceDetector, DrawBoundingBox drawBoundingBox, boolean Orientation) {
            this.faceDetector = faceDetector;
            this.drawBoundingBox = drawBoundingBox;
            this.Orientation = Orientation;
    }


    @Override
    public void analyze(@NonNull ImageProxy image) {
        @SuppressLint("UnsafeOptInUsageError") Image mediaImage = image.getImage();
        if(mediaImage != null) {
            InputImage inputImage = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
            Task<List<Face>> result =
                    faceDetector.process(inputImage)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<Face>>() {
                                        @Override
                                        public void onSuccess(List<Face> faces) {
                                            // Task completed successfully
                                            // ...a
                                            if(faces.isEmpty()) {
                                                drawBoundingBox.rect = new RectF(0, 0, 0, 0);
                                                drawBoundingBox.invalidate();
                                            }
                                            for (Face face : faces) {
                                                Rect bounds = face.getBoundingBox();
                                                // scaling is required because the image dimension and dimension of camera preview are different
                                                float scaleY = drawBoundingBox.getHeight()/(float)mediaImage.getWidth();
                                                float scaleX = drawBoundingBox.getWidth()/(float)mediaImage.getHeight();
                                                float left, right;
                                                // mirror the coordinates because of use of front camera
                                                if(Orientation == true) {
                                                    left = drawBoundingBox.getWidth() - (float) bounds.right * scaleX;
                                                    right = drawBoundingBox.getWidth() - (float) bounds.left * scaleX;
                                                }
                                                else{
                                                    left = bounds.left * scaleX;
                                                    right = bounds.right * scaleX;
                                                }

                                                //draw the rectangle
                                                drawBoundingBox.rect = new RectF(left, (float) bounds.top * scaleY, right, (float) bounds.bottom * scaleY);
                                                drawBoundingBox.invalidate();
                                            }

                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    });
            result.addOnCompleteListener(results->image.close());

        }
    }
}