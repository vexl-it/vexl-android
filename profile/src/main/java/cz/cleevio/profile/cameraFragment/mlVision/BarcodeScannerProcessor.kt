/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.cleevio.profile.cameraFragment.mlVision

import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import timber.log.Timber

class BarcodeScannerProcessor : VisionProcessorBase<List<Barcode>>() {

	var onBarcodesDetected: OnBarcodesDetected? = null

	private val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(
		BarcodeScannerOptions.Builder()
			.setBarcodeFormats(Barcode.FORMAT_QR_CODE)
			.build()
	)

	override fun stop() {
		super.stop()
		barcodeScanner.close()
	}

	override fun detectInImage(image: InputImage): Task<List<Barcode>> = barcodeScanner.process(image)

	override fun onSuccess(results: List<Barcode>) {
		onBarcodesDetected?.onBarcodesDetect(results)
	}

	override fun onFailure(e: Exception) {
		Timber.e(e)
	}

	interface OnBarcodesDetected {
		fun onBarcodesDetect(codes: List<Barcode>)
	}
}
