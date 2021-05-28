package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;

public class OpenQuestionDto extends QuestionDetailsDto {
    private String teacherAnswer;

    public OpenQuestionDto() {
    }

    public OpenQuestionDto(OpenQuestion question) {
        this.teacherAnswer = question.getTeacherAnswer();
    }

    public String getTeacherAnswer() {
        return teacherAnswer;
    }

    public void setTeacherAnswer(String teacherAnswer) {
        this.teacherAnswer = teacherAnswer;
    }

    @Override
    public QuestionDetails getQuestionDetails(Question question) {
        return new OpenQuestion(question, this);
    }

    @Override
    public void update(OpenQuestion question) {
        question.update(this);
    }

    @Override
    public String toString() {
        return "OpenQuestionDto{" +
                "teacherAnswer='" + teacherAnswer + '\'' +
                '}';
    }
}
