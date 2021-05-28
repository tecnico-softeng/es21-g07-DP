package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ConnectionItemDto;

import javax.persistence.*;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_CONTENT_FOR_OPTION;

@Entity
@Table(name = "connection_items")
public class ConnectionItem implements DomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_details_id")
    private ItemConnectionQuestion questionDetails;

    public ConnectionItem() {
    }

    public ConnectionItem(ConnectionItemDto option) {
        setContent(option.getContent());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitItem(this);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content == null || content.isBlank())
            throw new TutorException(INVALID_CONTENT_FOR_OPTION);

        this.content = content;
    }

    public void setQuestionDetails(ItemConnectionQuestion question, int group) {
        this.questionDetails = question;
        question.addItem(this, group);
    }

    public Integer getId(){
        return this.id;
    }

    @Override
    public String toString() {
        return "ConnectionItem{" +
                "content=" + content +
                ",id=" + id +
                '}';
    }

    public void remove() {
        this.questionDetails = null;
    }

}