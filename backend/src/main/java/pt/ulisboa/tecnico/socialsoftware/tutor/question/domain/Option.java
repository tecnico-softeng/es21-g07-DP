package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;


import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_CONTENT_FOR_OPTION;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_SEQUENCE_FOR_OPTION;

@Entity
@Table(name = "options")
public class Option implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer sequence;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private boolean correct;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "correct_order")
    private Integer order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_details_id")
    private MultipleChoiceQuestion questionDetails;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "option", fetch = FetchType.LAZY, orphanRemoval = true)
    private final Set<MultipleChoiceAnswer> questionAnswers = new HashSet<>();

    public Option() {
    }

    public Option(OptionDto option) {
        setSequence(option.getSequence());
        setContent(option.getContent());
        setCorrect(option.isCorrect());
        setOrder(option.getOrder());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitOption(this);
    }

    public Integer getId() {
        return id;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public void deleteOrder() {
        this.order = null;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        if (sequence == null || sequence < 0)
            throw new TutorException(INVALID_SEQUENCE_FOR_OPTION);

        this.sequence = sequence;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content == null || content.isBlank())
            throw new TutorException(INVALID_CONTENT_FOR_OPTION);

        this.content = content;
    }

    public MultipleChoiceQuestion getQuestionDetails() {
        return questionDetails;
    }

    public void setQuestionDetails(MultipleChoiceQuestion question) {
        this.questionDetails = question;
        question.addOption(this);
    }

    public Set<MultipleChoiceAnswer> getQuestionAnswers() {
        return questionAnswers;
    }

    public void addQuestionAnswer(MultipleChoiceAnswer questionAnswer) {
        questionAnswers.add(questionAnswer);
    }

    @Override
    public String toString() {
        if (order == null) {
            return "Option{" +
                    "id=" + id +
                    ", sequence=" + sequence +
                    ", order=None" +
                    ", correct=" + correct +
                    ", content='" + content + '\'' +
                    ", question=" + questionDetails.getId() +
                    ", questionAnswers=" + questionAnswers +
                    '}';
        } else {
            return "Option{" +
                    "id=" + id +
                    ", sequence=" + sequence +
                    ", order=" + order +
                    ", correct=" + correct +
                    ", content='" + content + '\'' +
                    ", question=" + questionDetails.getId() +
                    ", questionAnswers=" + questionAnswers +
                    '}';
        }
    }

    public void remove() {
        this.questionDetails = null;
        this.questionAnswers.clear();
    }
}