package com.example.quiz;

import static com.example.quiz.SetsActivity.setsIDs;
import static com.example.quiz.SplashActivity.catList;
import static com.example.quiz.SplashActivity.selected_cat_index;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.ArrayMap;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestionsActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView question, qcount,timer;
    private Button option1,option2,option3,option4;
    private List<Question> questionList;
    int quesNum;
    private CountDownTimer countDown;
    private int score;
    private FirebaseFirestore firestore;
    private int setNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        question=findViewById(R.id.question);
        qcount=findViewById(R.id.quest_num);
        timer=findViewById(R.id.countdown);

        option1=findViewById(R.id.option1);
        option2=findViewById(R.id.option2);
        option3=findViewById(R.id.option3);
        option4=findViewById(R.id.option4);

        option1.setOnClickListener(this);
        option2.setOnClickListener(this);
        option3.setOnClickListener(this);
        option4.setOnClickListener(this);

        questionList = new ArrayList<>();

        setNo = getIntent().getIntExtra("SETNO",1);
        firestore = FirebaseFirestore.getInstance();

        getQuestionsList();
        score=0;
    }

    private void getQuestionsList()
    {
        questionList.clear();

        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .collection(setsIDs.get(setNo)).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Map<String, QueryDocumentSnapshot> docList = new ArrayMap<>();

                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots)
                        {
                            docList.put(doc.getId(), doc);
                        }
                        QueryDocumentSnapshot quesListDoc = docList.get("QUESTIONS_LIST");
                        String count = quesListDoc.getString("COUNT");
                        for(int i=0;i<Integer.valueOf(count);i++)
                        {
                            String quesID = quesListDoc.getString("Q" + String.valueOf(i+1) + "_ID");

                            QueryDocumentSnapshot quesDoc = docList.get(quesID);
                            questionList.add(new Question(
                                    quesDoc.getString("QUESTION"),
                                    quesDoc.getString("A"),
                                    quesDoc.getString("B"),
                                    quesDoc.getString("C"),
                                    quesDoc.getString("D"),
                                    Integer.valueOf(quesDoc.getString("ANSWER"))
                            ));
                        }
                        setQuestion();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuestionsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setQuestion()
    {
        timer.setText(String.valueOf(10));
        question.setText(questionList.get(0).getQuestion());
        option1.setText(questionList.get(0).getOptionA());
        option2.setText(questionList.get(0).getOptionB());
        option3.setText(questionList.get(0).getOptionC());
        option4.setText(questionList.get(0).getOptionD());

        qcount.setText(String.valueOf(1)+"/"+ String.valueOf(questionList.size()));
        startTimer();
        quesNum=0;
    }

    private void startTimer()
    {
        countDown = new CountDownTimer(12000,1000) {
            @Override
            public void onTick(long l) {
                if(l<10000)
                    timer.setText(String.valueOf(l/1000));
            }

            @Override
            public void onFinish() {
                changeQuestion();
            }
        };
        countDown.start();
    }

    @Override
    public void onClick(View view) {

        int selectedOption=0;

        switch(view.getId())
        {
            case R.id.option1:
                selectedOption=1;
                break;

            case R.id.option2:
                selectedOption=2;
                break;

            case R.id.option3:
                selectedOption=3;
                break;

            case R.id.option4:
                selectedOption=4;
                break;

            default:
        }
        countDown.cancel();
        checkAnswer(selectedOption,view);
    }

    private void checkAnswer(int selectedOption, View view)
    {
        if(selectedOption==questionList.get(quesNum).getCorrectAns())
        {
            ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5BFF03")));
            score++;
        }
        else
        {
            ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0303")));
            switch (questionList.get(quesNum).getCorrectAns())
            {
                case 1:
                    option1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5BFF03")));
                    break;
                case 2:
                    option2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5BFF03")));
                    break;
                case 3:
                    option3.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5BFF03")));
                    break;
                case 4:
                    option4.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5BFF03")));
                    break;

            }
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                changeQuestion();
            }
        },2000);

    }

    private void changeQuestion()
    {
        if(quesNum<questionList.size()-1)
        {
            quesNum++;
            playAnim(question,0,0);
            playAnim(option1,0,1);
            playAnim(option2,0,2);
            playAnim(option3,0,3);
            playAnim(option4,0,4);

            qcount.setText(String.valueOf(quesNum+1)+"/"+String.valueOf(questionList.size()));
            timer.setText(String.valueOf(10));
            startTimer();

        }
        else
        {
            Intent intent= new Intent(QuestionsActivity.this,ScoreActivity.class);
            intent.putExtra("SCORE", String.valueOf(score)+"/"+String.valueOf(questionList.size()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            QuestionsActivity.this.finish();
        }
    }

    private void playAnim(View v,final int value, int viewNum)
    {
        v.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500)
                .setStartDelay(100).setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        if(value==0)
                        {
                            switch (viewNum)
                            {
                                case 0:
                                    ((TextView)v).setText(questionList.get(quesNum).getQuestion());
                                    break;
                                case 1:
                                    ((Button)v).setText(questionList.get(quesNum).getOptionA());
                                    break;
                                case 2:
                                    ((Button)v).setText(questionList.get(quesNum).getOptionB());
                                    break;
                                case 3:
                                    ((Button)v).setText(questionList.get(quesNum).getOptionC());
                                    break;
                                case 4:
                                    ((Button)v).setText(questionList.get(quesNum).getOptionD());
                                    break;
                            }
                            if(viewNum!=0)
                                ((Button)v).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FB8585")));
                            playAnim(v,1,viewNum);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        countDown.cancel();
    }

}