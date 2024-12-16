package com.isamm.weathapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class quiz extends AppCompatActivity implements View.OnClickListener{
    // decalation des variables
    TextView totalQuestionsTextView;
    TextView questionTextView;
    Button ansA, ansB, ansC, ansD;
    Button submitBtn;
    ActionBar actionBar;
    private FirebaseAuth firebaseAuth;
    private String fullName, doB, gender, mobile;

    static int score=0;
    int totalQuestion = QuestionAnswer.question.length;
    int currentQuestionIndex = 0;
    String selectedAnswer = "";
    TextView textView;
    int i = 2;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); //
        }
        //initialisation des valeurs
        totalQuestionsTextView = findViewById(R.id.total_question);
        questionTextView = findViewById(R.id.question);
        ansA = findViewById(R.id.ans_A);
        ansB = findViewById(R.id.ans_B);
        ansC = findViewById(R.id.ans_C);
        ansD = findViewById(R.id.ans_D);
        submitBtn = findViewById(R.id.submit_btn);

        //declencher un evenement lorsque on clique
        ansA.setOnClickListener(this);
        ansB.setOnClickListener(this);
        ansC.setOnClickListener(this);
        ansD.setOnClickListener(this);
        submitBtn.setOnClickListener(this);
        //donner la valeur du total question
        totalQuestionsTextView.setText("Total questions : "+totalQuestion);

        loadNewQuestion();

        FirebaseDatabase.getInstance().getReference("Registered Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ReadWriteUserDetails userDetails = snapshot.getValue(ReadWriteUserDetails.class);
                        if (userDetails != null) {
                            fullName = userDetails.getFullName();
                            doB = userDetails.getDoB();
                            gender = userDetails.getGender();
                            mobile = userDetails.getMobile();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });

    }

    @Override
    public void onClick(View view) {
        // Set the background color of button to white
        ansA.setBackgroundColor(Color.WHITE);
        ansB.setBackgroundColor(Color.WHITE);
        ansC.setBackgroundColor(Color.WHITE);
        ansD.setBackgroundColor(Color.WHITE);
        Button clickedButton = (Button) view;
        if(clickedButton.getId()==R.id.submit_btn){
            // Check if the selected answeris correct
            if(selectedAnswer.equals(QuestionAnswer.correctAnswers[currentQuestionIndex])){
                score++;
            }
            currentQuestionIndex++;
            loadNewQuestion();

        } else {
            //choices button clicked
            selectedAnswer  = clickedButton.getText().toString();
            clickedButton.setBackgroundColor(Color.MAGENTA);
        }
    }


    //lancer le question suivant
    void loadNewQuestion(){
        if(currentQuestionIndex == totalQuestion ){
            // Update quiz.score with the latest score value
            ReadWriteUserDetails userDetails = new ReadWriteUserDetails(fullName, doB, gender, mobile,score);
            userDetails.setLastScore(score);
            FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(userDetails);
            finishQuiz();
            return;
        }

        questionTextView.setText(QuestionAnswer.question[currentQuestionIndex]);
        ansA.setText(QuestionAnswer.choices[currentQuestionIndex][0]);
        ansB.setText(QuestionAnswer.choices[currentQuestionIndex][1]);
        ansC.setText(QuestionAnswer.choices[currentQuestionIndex][2]);
        ansD.setText(QuestionAnswer.choices[currentQuestionIndex][3]);

    }

    //finir le jeux
    void finishQuiz(){
        String passStatus = "";
        if(score > totalQuestion*0.60){
            passStatus = "Passed";
        }else{
            passStatus = "Failed";
        }

        new AlertDialog.Builder(this)
                .setTitle(passStatus)
                .setMessage("Score is "+ score+" out of "+ totalQuestion)
                .setPositiveButton("Restart",(dialogInterface, i) -> restartQuiz() )
                .setCancelable(false)
                .show();
    }


    void restartQuiz(){
        score = 0;
        currentQuestionIndex =0;
        loadNewQuestion();
    }
}