package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ConnectionLinkDto;

import javax.persistence.*;

@Entity
@Table(name = "connection_links")
public class ConnectionLink implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer origin;

    @Column(nullable = false)
    private Integer destiny;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_details_id")
    private ItemConnectionQuestion questionDetails;

    public ConnectionLink() {
    }

    public ConnectionLink(ConnectionLinkDto link) {
        this.origin = link.getOrigin();
        this.destiny = link.getDestiny();
    }
    
    public Integer getId() {
        return id;
    }

    public Integer getOrigin() {
        return origin;
    }

    public Integer getDestiny() {
        return destiny;
    }

    public void setOrigin(int origin) {
        this.origin= origin;
    }

    public void setDestiny(int destiny) {
        this.destiny = destiny;
    }

    public void setQuestionDetails(ItemConnectionQuestion question) {
        this.questionDetails = question;
        question.addLink(this);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitConnectionLink(this);
    }

    @Override
    public String toString() {
        return "ConnectionLink{" +
                "origin=" + origin +
                ",destiny=" + destiny +
                ",id=" + id +
                '}';
    }

    public void remove() {
        this.questionDetails = null;
    }
}