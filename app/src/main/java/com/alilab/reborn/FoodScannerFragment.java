package com.alilab.reborn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodScannerFragment extends Fragment {

    private static final String MODEL_FILENAME = "mobilenet_v2.tflite";
    private static final String LABELS_FILENAME = "labels.txt";
    private static final int INPUT_SIZE = 224;
    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;

    private ImageView ivFoodImage;
    private TextView tvPlaceholderHint;
    private TextView tvResultLabel;
    private TextView tvResultConfidence;
    private TextView tvResultSubtitle;

    private Bitmap selectedBitmap = null;
    private Interpreter interpreter = null;
    private List<String> labelList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null && extras.get("data") instanceof Bitmap) {
                        selectedBitmap = (Bitmap) extras.get("data");
                        displaySelectedImage();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        Context context = requireContext();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            selectedBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.getContentResolver(), imageUri));
                        } else {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                        }
                        // Ensure software bitmap for pixel processing
                        if (selectedBitmap != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && selectedBitmap.getConfig() == Bitmap.Config.HARDWARE) {
                            selectedBitmap = selectedBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        }
                        displaySelectedImage();
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Failed to load image from gallery", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_scanner, container, false);

        initViews(view);
        initTFLiteModel();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        ivFoodImage = view.findViewById(R.id.iv_food_image);
        tvPlaceholderHint = view.findViewById(R.id.tv_image_placeholder_hint);
        tvResultLabel = view.findViewById(R.id.tv_result_label);
        tvResultConfidence = view.findViewById(R.id.tv_result_confidence);
        tvResultSubtitle = view.findViewById(R.id.tv_result_subtitle);

        Button btnCapturePhoto = view.findViewById(R.id.btn_capture_photo);
        Button btnSelectGallery = view.findViewById(R.id.btn_select_gallery);
        Button btnScanFood = view.findViewById(R.id.btn_scan_food);

        btnCapturePhoto.setOnClickListener(v -> openCamera());
        btnSelectGallery.setOnClickListener(v -> openGallery());
        btnScanFood.setOnClickListener(v -> runFoodClassification());
    }

    private void initTFLiteModel() {
        if (getContext() == null) return;
        try {
            AssetManager assetManager = getContext().getAssets();
            MappedByteBuffer modelFile = loadModelFile(assetManager, MODEL_FILENAME);
            Interpreter.Options options = new Interpreter.Options();
            interpreter = new Interpreter(modelFile, options);

            labelList = loadLabelList(assetManager, LABELS_FILENAME);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error loading TFLite Model: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        List<String> labels = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    labels.add(trimmed);
                }
            }
        }
        return labels;
    }

    private void setupClickListeners() {
        // Additional listeners if needed
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void displaySelectedImage() {
        if (selectedBitmap != null) {
            ivFoodImage.setImageBitmap(selectedBitmap);
            tvPlaceholderHint.setVisibility(View.GONE);
            tvResultLabel.setText("Image Ready! Tap 'Scan Food'");
            tvResultConfidence.setText("Ready");
        }
    }

    private void runFoodClassification() {
        if (selectedBitmap == null) {
            Toast.makeText(getContext(), "Please capture or select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (interpreter == null || labelList.isEmpty()) {
            Toast.makeText(getContext(), "Model not loaded. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Resize Bitmap to 224x224
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(selectedBitmap, INPUT_SIZE, INPUT_SIZE, true);

        // 2. Preprocess Bitmap into Normalized ByteBuffer
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3);
        inputBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        resizedBitmap.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

        inputBuffer.rewind();
        for (int pixel : intValues) {
            float r = (((pixel >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
            float g = (((pixel >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
            float b = ((pixel & 0xFF) - IMAGE_MEAN) / IMAGE_STD;

            inputBuffer.putFloat(r);
            inputBuffer.putFloat(g);
            inputBuffer.putFloat(b);
        }

        // 3. Prepare Output Array
        int numClasses = labelList.size();
        float[][] outputArray = new float[1][numClasses];

        // 4. Run Inference
        interpreter.run(inputBuffer, outputArray);

        // 5. Argmax & Confidence Extraction
        int maxIndex = -1;
        float maxConfidence = -1.0f;

        for (int i = 0; i < numClasses; i++) {
            if (outputArray[0][i] > maxConfidence) {
                maxConfidence = outputArray[0][i];
                maxIndex = i;
            }
        }

        // 6. Display Prediction Results
        if (maxIndex >= 0 && maxIndex < labelList.size()) {
            String predictedLabel = labelList.get(maxIndex);

            // Calculate confidence percentage
            float confidencePct = maxConfidence;
            // If model output is probability [0..1]
            if (maxConfidence <= 1.0f && maxConfidence >= 0.0f) {
                confidencePct = maxConfidence * 100.0f;
            }

            // Format label nicely (capitalized)
            String formattedLabel = capitalizeWords(predictedLabel);

            tvResultLabel.setText(formattedLabel);
            tvResultConfidence.setText(String.format(Locale.getDefault(), "%.1f%% Confidence", confidencePct));
            tvResultSubtitle.setText(String.format(Locale.getDefault(), "Detected with MobileNet V2 Vision (Class #%d)", maxIndex));

            Toast.makeText(getContext(), "Detected: " + formattedLabel, Toast.LENGTH_SHORT).show();
        } else {
            tvResultLabel.setText("Unable to identify food");
            tvResultConfidence.setText("Low Confidence");
        }
    }

    private String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) return "";
        String[] words = input.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}
