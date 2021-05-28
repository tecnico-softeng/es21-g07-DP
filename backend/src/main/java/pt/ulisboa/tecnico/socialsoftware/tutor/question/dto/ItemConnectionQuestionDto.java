package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.*;

import java.util.*;
import java.util.stream.Collectors;

public class ItemConnectionQuestionDto extends QuestionDetailsDto{
    private List<ConnectionItemDto> groupRight = new ArrayList<>();
    private List<ConnectionItemDto> groupLeft = new ArrayList<>();
    private List<ConnectionLinkDto> connections = new ArrayList<>();


    public ItemConnectionQuestionDto() { }

    public ItemConnectionQuestionDto(ItemConnectionQuestion question) {
        List<List<ConnectionItem>> items = question.getOptions();
        List<ConnectionLink> links = question.getConnections();

        for (int i = 0; i < items.get(0).size(); i++) {
            this.groupLeft.add(new ConnectionItemDto(items.get(0).get(i)));
        }
        for (int i = 0; i < items.get(1).size(); i++){
            this.groupRight.add(new ConnectionItemDto(items.get(1).get(i)));
        }
        for (int i = 0; i < links.size(); i++){
            this.connections.add(new ConnectionLinkDto(links.get(i)));
        }
    }

    @Override
    public QuestionDetails getQuestionDetails(Question question) {
        return new ItemConnectionQuestion(question, this);
    }

    public List<ConnectionItemDto> getGroupLeft() {
        return this.groupLeft;
    }
    public List<ConnectionItemDto> getGroupRight() {
        return this.groupRight;
    }

    public void setOptions(List<ConnectionItemDto> leftGroup, List<ConnectionItemDto> rightGroup) {
        this.groupLeft = leftGroup;
        this.groupRight = rightGroup;
    }

    public void setConnections(List<ConnectionLinkDto> links) {
        this.connections.addAll(links);
    }

    public  List<ConnectionLinkDto> getConnections() {
        return this.connections;
    }

    @Override
    public void update(ItemConnectionQuestion question) {
        question.update(this);
    }
}
