package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.Updator;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDetailsDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@DiscriminatorValue(Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION)
public class MultipleChoiceQuestion extends QuestionDetails {
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionDetails", fetch = FetchType.EAGER, orphanRemoval = true)
    private final List<Option> options = new ArrayList<>();

    public MultipleChoiceQuestion() {
        super();
    }

    public MultipleChoiceQuestion(Question question, MultipleChoiceQuestionDto questionDto) {
        super(question);
        setOptions(questionDto.getOptions());
    }

    public List<Option> getOptions() {
        return options;
    }

    public void checkNumberOfCorrectOptions(List<OptionDto> optionDtos) {
        if (optionDtos.stream().filter(OptionDto::isCorrect).count() < 1) {
            throw new TutorException(ONE_CORRECT_OPTION_NEEDED);
        }
    }

    // Order question: If there is less than 3 options with order but there is AT LEAST 1 true option with order
    public void hasAtLeastThreeOptionsOrdered(List<OptionDto> options) {
        if (options.stream().filter(x -> x.getOrder() != null).count() < 3 && options.stream().filter(x -> x.getOrder() != null && x.isCorrect()).count() != 0) {
            throw new TutorException(AT_LEAST_THREE_OPTIONS_ORDERED);
        }
    }

    // Order question: if the question has an option with order AND there is one true option without order
    public void allCorrectOptionsHaveOrder(List<OptionDto> options) {
        if (options.stream().filter(x -> x.getOrder() != null && x.isCorrect()).count() > 1 && options.stream().filter(x -> x.getOrder() == null && x.isCorrect()).count() > 0) {
            throw new TutorException(NOT_ALL_HAVE_ORDER);
        }
    }

    // Order question: we need to have a correct order! 1 -> 2 -> 3 -> ...
    public void orderOfCorrectOptionsIsCorrect(List<OptionDto> options) {
        if (options.stream().filter(x -> x.getOrder() != null && x.isCorrect()).count() > 1) {
            for (int i = 1; i <= options.stream().filter(OptionDto::isCorrect).count(); i++) {
                final Integer order = i;
                if (options.stream().filter(x -> Objects.equals(x.getOrder(), order) && x.isCorrect()).count() != 1) {
                    throw new TutorException(INCORRECT_ORDER);
                }
            }
        }
    }

    public void setOptions(List<OptionDto> options) {
        checkNumberOfCorrectOptions(options);
        hasAtLeastThreeOptionsOrdered(options);
        allCorrectOptionsHaveOrder(options);
        orderOfCorrectOptionsIsCorrect(options);

        for (Option option: this.options) {
            option.remove();
        }
        this.options.clear();

        int index = 0;
        for (OptionDto optionDto : options) {
            optionDto.setSequence(index++);
            new Option(optionDto).setQuestionDetails(this);
        }
    }

    public void addOption(Option option) {
        options.add(option);
    }

    public Integer getCorrectOptionId() {
        return this.getOptions().stream()
                .filter(Option::isCorrect)
                .findAny()
                .map(Option::getId)
                .orElse(null);
    }

    // Iterates the options of the question and returns a list with all the correct ones
    public List<Option> getAllCorrectOptions() {
        Stream<Option> listStream = this.getOptions().stream().filter(Option::isCorrect);               // Getting all the questions
        return listStream.collect(Collectors.toList());                               // Converting for List for easier management
    }

    public void update(MultipleChoiceQuestionDto questionDetails) {
        setOptions(questionDetails.getOptions());
    }

    @Override
    public void update(Updator updator) {
        updator.update(this);
    }

    @Override
    public String getCorrectAnswerRepresentation() {
        return convertSequenceToLetter(this.getCorrectAnswer());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitQuestionDetails(this);
    }

    public void visitOptions(Visitor visitor) {
        for (Option option : this.getOptions()) {
            option.accept(visitor);
        }
    }

    @Override
    public CorrectAnswerDetailsDto getCorrectAnswerDetailsDto() {
        return new MultipleChoiceCorrectAnswerDto(this);
    }

    @Override
    public StatementQuestionDetailsDto getStatementQuestionDetailsDto() {
        return new MultipleChoiceStatementQuestionDetailsDto(this);
    }

    @Override
    public StatementAnswerDetailsDto getEmptyStatementAnswerDetailsDto() {
        return new MultipleChoiceStatementAnswerDetailsDto();
    }

    @Override
    public AnswerDetailsDto getEmptyAnswerDetailsDto() {
        return new MultipleChoiceAnswerDto();
    }

    @Override
    public QuestionDetailsDto getQuestionDetailsDto() {
        return new MultipleChoiceQuestionDto(this);
    }

    public Integer getCorrectAnswer() {
        return this.getOptions()
                .stream()
                .filter(Option::isCorrect)
                .findAny().orElseThrow(() -> new TutorException(NO_CORRECT_OPTION))
                .getSequence();
    }

    @Override
    public void delete() {
        super.delete();
        for (Option option : this.options) {
            option.remove();
        }
        this.options.clear();
    }

    @Override
    public String toString() {
        return "MultipleChoiceQuestion{" +
                "options=" + options +
                '}';
    }

    public static String convertSequenceToLetter(Integer correctAnswer) {
        return correctAnswer != null ? Character.toString('A' + correctAnswer) : "-";
    }

    @Override
    public String getAnswerRepresentation(List<Integer> selectedIds) {
        var result = this.options
                .stream()
                .filter(x -> selectedIds.contains(x.getId()))
                .map(x -> convertSequenceToLetter(x.getSequence()))
                .collect(Collectors.joining("|"));
        return !result.isEmpty() ? result : "-";
    }
}
