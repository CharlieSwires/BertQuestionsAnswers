package com.charlie;

// Bean class to hold the incoming JSON
public class QuestionRequest {

    private String question;
    private String context;

    // Default constructor for JSON mapping
    public QuestionRequest() {
    }

    public QuestionRequest(String question, String context) {
        this.question = question;
        this.context = context;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "QuestionRequest{question='" + question + "', context='" + context + "'}";
    }
}