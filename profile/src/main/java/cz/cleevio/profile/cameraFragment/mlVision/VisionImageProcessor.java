package cz.cleevio.profile.cameraFragment.mlVision;

import android.graphics.Bitmap;

public interface VisionImageProcessor {

	void processBitmap(Bitmap bitmap);

	void stop();
}
