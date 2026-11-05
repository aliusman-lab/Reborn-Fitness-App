package com.alilab.reborn;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AiAssistantFragment extends Fragment {

    private static final String PREF_NAME = "REBORN_PREFS";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_GOAL = "user_goal";

    private RecyclerView rvChatHistory;
    private EditText etInput;
    private ImageButton btnSend;

    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    private String userName = "Athlete";
    private String userGoal = "Fitness";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_assistant, container, false);

        initViews(view);
        loadUserData();
        setupRecyclerView();

        if (messageList.isEmpty()) {
            addAiWelcomeMessage();
        }

        btnSend.setOnClickListener(v -> handleSendMessage());

        return view;
    }

    private void initViews(View view) {
        rvChatHistory = view.findViewById(R.id.rv_chat_history);
        etInput = view.findViewById(R.id.et_chat_input);
        btnSend = view.findViewById(R.id.btn_send_chat);
    }

    private void loadUserData() {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            userName = prefs.getString(KEY_USER_NAME, "Athlete");
            userGoal = prefs.getString(KEY_USER_GOAL, "Weight Loss");
        }
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvChatHistory.setLayoutManager(layoutManager);
        rvChatHistory.setAdapter(chatAdapter);
    }

    private void addAiWelcomeMessage() {
        String welcome = String.format("Hello %s! 👋 I am your REBORN AI Coach. I see your current goal is '%s'. How can I assist your nutrition or workout routine today?", userName, userGoal);
        messageList.add(new ChatMessage(welcome, false, getCurrentTime()));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
    }

    private void handleSendMessage() {
        String query = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(query)) {
            Toast.makeText(getContext(), "Please type a prompt for AI Assistant", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user message
        messageList.add(new ChatMessage(query, true, getCurrentTime()));
        int userPos = messageList.size() - 1;
        chatAdapter.notifyItemInserted(userPos);
        rvChatHistory.smoothScrollToPosition(userPos);

        etInput.setText("");

        // Simulate AI thinking and reply
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String aiResponse = generateSmartAiResponse(query);
            messageList.add(new ChatMessage(aiResponse, false, getCurrentTime()));
            int aiPos = messageList.size() - 1;
            chatAdapter.notifyItemInserted(aiPos);
            rvChatHistory.smoothScrollToPosition(aiPos);
        }, 600);
    }

    private String generateSmartAiResponse(String query) {
        String lower = query.toLowerCase(Locale.ROOT);

        if (lower.contains("protein")) {
            return String.format(Locale.getDefault(), "%s, for optimal muscle recovery and satiety during %s, aim for 1.6g - 2.2g of protein per kg of body weight daily. Lean chicken, eggs, whey, Greek yogurt, and tofu are excellent sources!", userName, userGoal);
        } else if (lower.contains("diet") || lower.contains("nutrition") || lower.contains("meal") || lower.contains("food")) {
            return String.format(Locale.getDefault(), "A balanced diet for %s consists of 30%% protein, 50%% complex carbohydrates (oats, brown rice, sweet potatoes), and 20%% healthy fats (avocado, nuts, olive oil). Consistency is key!", userGoal);
        } else if (lower.contains("workout") || lower.contains("exercise") || lower.contains("split") || lower.contains("routine")) {
            return "For best results, try a Push-Pull-Legs (PPL) or Upper-Lower split 4-5 times a week. Focus on compound movements like squats, bench presses, deadlifts, and overhead presses with progressive overload!";
        } else if (lower.contains("weight loss") || lower.contains("fat") || lower.contains("cut")) {
            return "To lose fat effectively while preserving muscle, maintain a moderate 300-500 kcal daily deficit, prioritize high protein intake, and incorporate 8,000-10,000 daily steps along with resistance training.";
        } else if (lower.contains("muscle") || lower.contains("gain") || lower.contains("bulk")) {
            return "Building clean lean muscle requires a slight caloric surplus (~300 kcal), progressive resistance training targeting each muscle group twice weekly, and at least 7-8 hours of quality sleep.";
        } else if (lower.contains("water") || lower.contains("hydration") || lower.contains("drink")) {
            return "Hydration is essential for peak physical performance! Aim to drink at least 3.5 to 4 liters of water daily, especially before and after intense workout sessions.";
        } else if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return String.format("Hey %s! Ready to smash your fitness goals today? Ask me anything about your diet, workouts, or recovery strategy!", userName);
        } else {
            return String.format("Great question, %s! Remember that staying consistent with your daily targets (%s) and prioritizing sleep and progressive overload will accelerate your transformation. Keep pushing!", userName, userGoal);
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date());
    }
}
