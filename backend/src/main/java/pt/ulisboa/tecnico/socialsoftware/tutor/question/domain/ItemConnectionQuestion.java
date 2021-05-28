package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.AnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuestionDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.Updator;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.*;

import javax.persistence.*;
import java.util.*;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.NOT_ENOUGH_OPTIONS_IN_GROUPS;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.NO_CONNECTIONS_GIVEN;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_CONNECTIONS_GIVEN;


@Entity
@DiscriminatorValue(Question.QuestionTypes.ITEM_CONNECTION_QUESTION)
public class ItemConnectionQuestion extends QuestionDetails {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private final List<ConnectionItem> groupRight = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private final List<ConnectionItem> groupLeft = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private final List<ConnectionLink> connections = new ArrayList<>();


    public ItemConnectionQuestion() { super(); }

    public ItemConnectionQuestion(Question question, ItemConnectionQuestionDto itemConnectionQuestionDto) {
        super(question);
        List<List<ConnectionItemDto>> arr = new ArrayList<>();
        arr.add(itemConnectionQuestionDto.getGroupLeft());
        arr.add(itemConnectionQuestionDto.getGroupRight());
        setOptions(arr);
        setConnections(itemConnectionQuestionDto.getConnections());
    }

    public void setOptions(List<List<ConnectionItemDto>> options) {
        for (ConnectionItem option: this.groupLeft) {
            option.remove();
        }
        for (ConnectionItem option: this.groupRight) {
            option.remove();
        }
        this.groupLeft.clear();
        this.groupRight.clear();
        if (!((options.get(0).size() > 1 && !options.get(1).isEmpty() ) ||
                (options.get(1).size() > 1 && !options.get(0).isEmpty() ))){
            throw new TutorException(NOT_ENOUGH_OPTIONS_IN_GROUPS);
        }
        for (ConnectionItemDto optionDto : options.get(1)) {
            ConnectionItem item = new ConnectionItem(optionDto);
            item.setQuestionDetails(this, 1);
        }
        for (ConnectionItemDto optionDto : options.get(0)) {
            ConnectionItem item = new ConnectionItem(optionDto);
            item.setQuestionDetails(this, 0);        
        }
    }

    public List<List<ConnectionItem>> getOptions() {
        List<List<ConnectionItem>> res = new ArrayList<>();
        res.add(this.groupLeft);
        res.add(this.groupRight);
        return res;
    }

    public void setConnections(List<ConnectionLinkDto> connections) {
        for (ConnectionLink option: this.connections) {
            option.remove();
        }
        this.connections.clear();
        if (connections.isEmpty()){
            throw new TutorException(NO_CONNECTIONS_GIVEN);
        }
        for (ConnectionLinkDto con: connections){
            if( !(con.getOrigin() < this.groupLeft.size() &&
                    con.getDestiny() < this.groupRight.size())){
                throw new TutorException(INVALID_CONNECTIONS_GIVEN);
            }
        }
        for (ConnectionLinkDto link : connections){
            ConnectionLink newLink = new ConnectionLink(link);
            newLink.setQuestionDetails(this);
        }
    }

    public List<ConnectionLink> getConnections() {
        return connections;
    }

    public void addItem(ConnectionItem item, int group) {
        if (group == 1){
            this.groupRight.add(item);
        }else{
            this.groupLeft.add(item);
        }

    }

    public void addLink(ConnectionLink link) {
       this.connections.add(link);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitQuestionDetails(this);
    }

    public void visitLeftItems(Visitor visitor) {
        for (ConnectionItem item : this.groupLeft) {
            item.accept(visitor);
        }

    }

    public void visitRightItems(Visitor visitor) {
        for (ConnectionItem item : this.groupRight) {
            item.accept(visitor);
        }
    }

    public void visitConnectionLinks(Visitor visitor) {
        for (ConnectionLink link : this.connections) {
            link.accept(visitor);
        }
    }

    @Override
    public CorrectAnswerDetailsDto getCorrectAnswerDetailsDto() {
        return null;
    }

    @Override
    public StatementQuestionDetailsDto getStatementQuestionDetailsDto() {
        return null;
    }

    @Override
    public StatementAnswerDetailsDto getEmptyStatementAnswerDetailsDto() {
        return null;
    }

    @Override
    public AnswerDetailsDto getEmptyAnswerDetailsDto() {
        return null;
    }

    @Override
    public ItemConnectionQuestionDto getQuestionDetailsDto() {
        return new ItemConnectionQuestionDto(this);
    }

    public void update(ItemConnectionQuestionDto questionDetails) {
        List<List<ConnectionItemDto>> arr = new ArrayList<>();
        arr.add(questionDetails.getGroupLeft());
        arr.add(questionDetails.getGroupRight());
        setOptions(arr);
        setConnections(questionDetails.getConnections());
    }

    @Override
    public void update(Updator updator) {
        updator.update(this);
    }

    @Override
    public String getCorrectAnswerRepresentation() {
        return null;
    }

    @Override
    public String getAnswerRepresentation(List<Integer> selectedIds) {
        return null;
    }

     @Override
    public String toString() {
        return "ItemConnectionQuestion{" +
                "groupLeft=" + groupRight +
                "groupRight=" + groupRight +
                "links=" + connections +
                '}';
    }
}
