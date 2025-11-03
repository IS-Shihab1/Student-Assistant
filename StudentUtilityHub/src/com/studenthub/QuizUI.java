package com.studenthub;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import com.studenthub.models.QuizQuestion;

public class QuizUI extends BaseUI {
    private JPanel panel;
    private java.util.List<QuizQuestion> questions;
    private int current = 0;
    private ButtonGroup grp;
    private JPanel questionArea;
    // exam state
    private boolean inExam = false;
    private java.util.List<QuizQuestion> examQuestions;
    private java.util.Map<Integer, String> userAnswers = new java.util.HashMap<>();

    public QuizUI(String username) {
        super(username);
    panel = new JPanel(new BorderLayout(6,6));
    Color bg = new Color(176, 206, 136);
    panel.setBackground(bg);
    questionArea = new JPanel(new BorderLayout());
    questionArea.setBackground(bg);
    panel.add(questionArea, BorderLayout.CENTER);
    JPanel controls = new JPanel();
    controls.setBackground(bg);
        JButton prev = new JButton("Prev");
        JButton next = new JButton("Next");
        JButton submit = new JButton("Submit");
        controls.add(prev); controls.add(next); controls.add(submit);
        panel.add(controls, BorderLayout.SOUTH);

    // admin / upload UI at top
    JPanel admin = new JPanel(new GridLayout(3,1,4,4));
    admin.setBackground(bg);
    admin.add(new JLabel("Add question (enter question, 3 options, and select correct answer):"));
    JTextField qField = new JTextField(40);
    admin.add(qField);
    JPanel opts = new JPanel(new FlowLayout(FlowLayout.LEFT,6,6));
    opts.setBackground(bg);
    JTextField o1 = new JTextField(12);
    JTextField o2 = new JTextField(12);
    JTextField o3 = new JTextField(12);
    JComboBox<String> correct = new JComboBox<>(new String[]{"Option 1","Option 2","Option 3"});
    JButton uploadQ = new JButton("Upload Question");
    JButton startExamBtn = new JButton("Start Exam");
    opts.add(new JLabel("Opt1:")); opts.add(o1);
    opts.add(new JLabel("Opt2:")); opts.add(o2);
    opts.add(new JLabel("Opt3:")); opts.add(o3);
    opts.add(new JLabel("Correct:")); opts.add(correct);
    opts.add(uploadQ); opts.add(startExamBtn);
    admin.add(opts);
    panel.add(admin, BorderLayout.NORTH);

    loadQuestions();
    showQuestion(0);

        prev.addActionListener(e -> {
            saveCurrentAnswer();
            showQuestion(current-1);
        });
        next.addActionListener(e -> {
            saveCurrentAnswer();
            showQuestion(current+1);
        });
        submit.addActionListener(e -> finishQuiz());

        // upload question handler
        uploadQ.addActionListener(e -> {
            String qtxt = qField.getText().trim();
            String s1 = o1.getText().trim();
            String s2 = o2.getText().trim();
            String s3 = o3.getText().trim();
            if (qtxt.isEmpty() || s1.isEmpty() || s2.isEmpty() || s3.isEmpty()) { JOptionPane.showMessageDialog(panel, "Fill question and all three options"); return; }
            int idx = correct.getSelectedIndex();
            String answer = idx==0?s1: idx==1?s2: s3;
            String line = String.join("|", qtxt, s1+";;"+s2+";;"+s3, answer);
            Utils.appendLine(new File(Utils.DATA_FOLDER, "quiz_questions.txt"), line);
            JOptionPane.showMessageDialog(panel, "Question uploaded");
            // clear inputs and reload
            qField.setText(""); o1.setText(""); o2.setText(""); o3.setText(""); correct.setSelectedIndex(0);
            loadQuestions();
        });

        startExamBtn.addActionListener(e -> startExam());
    }

    private void loadQuestions() {
        try {
            // prefer user-provided file in data folder
            File f = new File(Utils.DATA_FOLDER, "quiz_questions.txt");
            questions = new ArrayList<>();
            if (f.exists()) {
                java.util.List<String> lines = Utils.readAllLines(f);
                for (String l : lines) {
                    if (l == null || l.trim().isEmpty()) continue;
                    try {
                        String[] parts = l.split("\\|");
                        String qtxt = parts.length>0?parts[0]:"";
                        String[] opts = parts.length>1?parts[1].split(";;"): new String[]{"","",""};
                        String ans = parts.length>2?parts[2]:"";
                        questions.add(new QuizQuestion(qtxt, Arrays.asList(opts), ans));
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }
            // if still empty, add sample fallback
            if (questions.isEmpty()) {
                questions.add(new QuizQuestion("2+2=?", Arrays.asList("3","4","5"), "4"));
                questions.add(new QuizQuestion("Capital of France?", Arrays.asList("Paris","Rome","London"), "Paris"));
            }
        } catch (Exception ex) { ex.printStackTrace(); questions = new ArrayList<>(); }
    }

    private void showQuestion(int idx) {
        if(idx < 0 || idx >= questions.size()) return;
        current = idx;
        questionArea.removeAll();
        QuizQuestion q = inExam ? examQuestions.get(idx) : questions.get(idx);
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(questionArea.getBackground());
        top.add(new JLabel((idx+1) + ". " + q.getQuestion()), BorderLayout.NORTH);
        JPanel opts = new JPanel(new GridLayout(q.getOptions().size(),1));
        opts.setBackground(questionArea.getBackground());
        grp = new ButtonGroup();
        for(String o : q.getOptions()) {
            JRadioButton r = new JRadioButton(o);
            r.setActionCommand(o);
            r.setOpaque(false);
            grp.add(r); opts.add(r);
            // if exam mode and user already answered this index, pre-select
            if (inExam) {
                String prev = userAnswers.get(idx);
                if (prev != null && prev.equals(o)) {
                    r.setSelected(true);
                }
            }
        }
        top.add(opts, BorderLayout.CENTER);
        questionArea.add(top, BorderLayout.CENTER);
        questionArea.revalidate(); questionArea.repaint();
    }

    private void finishQuiz() {
        if (!inExam) {
            // non-exam submit: keep existing placeholder behavior
            String line = String.join(",", username, "0", new Date().toString());
            Utils.appendLine(new File(Utils.DATA_FOLDER, "results.csv"), line);
            JOptionPane.showMessageDialog(panel, "Quiz saved (score placeholder). Extend scoring logic per-question if needed.");
            return;
        }
        // exam submit: evaluate answers
        saveCurrentAnswer();
        int correct = 0;
        int total = examQuestions.size();
        for (int i = 0; i < total; i++) {
            String sel = userAnswers.get(i);
            String ans = examQuestions.get(i).getAnswer();
            if (sel != null && sel.equals(ans)) correct++;
        }
        String line = String.join(",", username, String.valueOf(correct), new Date().toString());
        Utils.appendLine(new File(Utils.DATA_FOLDER, "results.csv"), line);
        JOptionPane.showMessageDialog(panel, String.format("Exam finished: %d / %d correct", correct, total));
        // exit exam mode
        inExam = false; examQuestions = null; userAnswers.clear();
    }

    private void startExam() {
        if (questions == null || questions.isEmpty()) { JOptionPane.showMessageDialog(panel, "No questions available. Add some first."); return; }
        inExam = true;
        examQuestions = new ArrayList<>(questions);
        java.util.Collections.shuffle(examQuestions);
        userAnswers.clear();
        current = 0;
        showQuestion(current);
        JOptionPane.showMessageDialog(panel, "Exam started. Answer the questions and click Submit when done.");
    }

    private void saveCurrentAnswer() {
        if (!inExam || grp == null) return;
        ButtonModel bm = grp.getSelection();
        if (bm != null) {
            userAnswers.put(current, bm.getActionCommand());
        }
    }

    @Override
    public JPanel getPanel() { return panel; }
}
