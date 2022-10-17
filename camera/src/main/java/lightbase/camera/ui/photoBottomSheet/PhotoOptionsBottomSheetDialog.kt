package lightbase.camera.ui.photoBottomSheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import lightbase.camera.databinding.BottomSheetPhotoOptionsBinding

class PhotoOptionsBottomSheetDialog : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetPhotoOptionsBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = BottomSheetPhotoOptionsBinding.inflate(layoutInflater, container, false)

		binding.close.setOnClickListener { dismiss() }

		binding.profileTakeAPhoto.apply {
			setOnClickListener {
				setFragmentResult(
					RESULT_PHOTO_OPTIONS_RESULT,
					bundleOf(RESULT_PHOTO_OPTIONS_RESULT to PhotoClickOptions.TAKE_A_PICTURE)
				)
				findNavController().popBackStack()
			}
		}

		binding.profilePhotoLibrary.apply {
			setOnClickListener {
				setFragmentResult(
					RESULT_PHOTO_OPTIONS_RESULT,
					bundleOf(RESULT_PHOTO_OPTIONS_RESULT to PhotoClickOptions.PICK_FROM_PHOTO_LIBRARY)
				)
				findNavController().popBackStack()
			}
		}

		return binding.root
	}

	override fun onStop() {
		dismiss()
		super.onStop()
	}

	companion object {
		const val RESULT_PHOTO_OPTIONS_RESULT = "result_photo_options_result"
	}
}
