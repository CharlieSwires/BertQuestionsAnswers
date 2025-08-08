package com.charlie;

//Bean class to hold the incoming JSON
public class QuestionRequest {
 private String question;

 // Default constructor for JSON mapping
 public QuestionRequest() {}

 public QuestionRequest(String question) {
     this.question = question;
 }

 public String getQuestion() {
     return question;
 }

 public void setQuestion(String question) {
     this.question = question;
 }

 @Override
 public String toString() {
     return "QuestionRequest{question='" + question + "'}";
 }
}
