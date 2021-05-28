package pt.ulisboa.tecnico.socialsoftware.tutor.question.service.pem

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class UpdatePemOrderingTest extends SpockTest {
    def question
    def optionOK
    def optionOK1
    def optionOK2
    def optionOK3
    def optionOK4
    def user

    def setup() {
        createExternalCourseAndExecution()
        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        and: 'an image'
        def image = new Image()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        imageRepository.save(image)

        given: "create a question"
        question = new Question()
        question.setCourse(externalCourse)
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setNumberOfAnswers(5)
        question.setNumberOfCorrect(4)
        question.setImage(image)
        def questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'five options'
        optionOK = new Option()
        optionOK.setContent(OPTION_1_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        optionOK.setOrder(1)
        optionOK.setQuestionDetails(questionDetails)
        optionRepository.save(optionOK)

        optionOK1 = new Option()
        optionOK1.setContent(OPTION_2_CONTENT)
        optionOK1.setCorrect(true)
        optionOK1.setSequence(1)
        optionOK1.setOrder(4)
        optionOK1.setQuestionDetails(questionDetails)
        optionRepository.save(optionOK1)

        optionOK2 = new Option()
        optionOK2.setContent(OPTION_3_CONTENT)
        optionOK2.setCorrect(true)
        optionOK2.setSequence(2)
        OptionOK2.setOrder(2)
        optionOK2.setQuestionDetails(questionDetails)
        optionRepository.save(optionOK2)

        optionOK3 = new Option()
        optionOK3.setContent(OPTION_4_CONTENT)
        optionOK3.setCorrect(true)
        optionOK3.setSequence(3)
        optionOK3.setOrder(3)
        optionOK3.setQuestionDetails(questionDetails)
        optionRepository.save(optionOK3)

        optionOK4 = new Option()
        optionOK4.setContent(OPTION_4_CONTENT)
        optionOK4.setCorrect(false)
        optionOK4.setSequence(4)
        optionOK4.setOrder(4)
        optionOK4.setQuestionDetails(questionDetails)
        optionRepository.save(optionOK4)
    }

    def "update question, change order"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: '1 option changed'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK1)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK2)
        optionDto.setOrder(3)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK3)
        optionDto.setOrder(2)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK4)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        and: 'a count to load options to memory due to in memory databse flaw'
        optionRepository.count()

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        result.getNumberOfCorrect() == 4
        result.getDifficulty() == 80
        and: 'are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getNumberOfAnswers() == 5
        result.getImage() != null
        and: 'an option is changed'
        result.getQuestionDetails().getOptions().size() == 5
        def resOptionOne = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK.getSequence()}).findAny().orElse(null)
        resOptionOne.getContent() == OPTION_1_CONTENT
        resOptionOne.getOrder() == 1
        resOptionOne.isCorrect()
        def resOptionTwo = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK1.getSequence()}).findAny().orElse(null)
        resOptionTwo.getContent() == OPTION_2_CONTENT
        resOptionTwo.getOrder() == 4
        resOptionTwo.isCorrect()
        def resOptionThree = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK2.getSequence()}).findAny().orElse(null)
        resOptionThree.getContent() == OPTION_3_CONTENT
        resOptionThree.getOrder() == 3
        resOptionThree.isCorrect()
        def resOptionFour = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK3.getSequence()}).findAny().orElse(null)
        resOptionFour.getContent() == OPTION_4_CONTENT
        resOptionFour.getOrder() == 2
        resOptionFour.isCorrect()
        def resOptionFive = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK4.getSequence()}).findAny().orElse(null)
        resOptionFive.getContent() == OPTION_4_CONTENT
        !resOptionFive.isCorrect()
    }

    def "update question, add element at the end"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setNumberOfCorrect(5)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: '1 option added'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK1)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK2)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK3)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK4)
        optionDto.setCorrect(true)
        optionDto.setOrder(5)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        and: 'a count to load options to memory due to in memory databse flaw'
        optionRepository.count()

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        result.getNumberOfCorrect() == 5
        result.getDifficulty() == 100
        and: 'are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getNumberOfAnswers() == 5
        result.getImage() != null
        and: 'an option is added'
        result.getQuestionDetails().getOptions().size() == 5
        def resOptionOne = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK.getSequence()}).findAny().orElse(null)
        resOptionOne.getContent() == OPTION_1_CONTENT
        resOptionOne.getOrder() == 1
        resOptionOne.isCorrect()
        def resOptionTwo = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK1.getSequence()}).findAny().orElse(null)
        resOptionTwo.getContent() == OPTION_2_CONTENT
        resOptionTwo.getOrder() == 4
        resOptionTwo.isCorrect()
        def resOptionThree = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK2.getSequence()}).findAny().orElse(null)
        resOptionThree.getContent() == OPTION_3_CONTENT
        resOptionThree.getOrder() == 2
        resOptionThree.isCorrect()
        def resOptionFour = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK3.getSequence()}).findAny().orElse(null)
        resOptionFour.getContent() == OPTION_4_CONTENT
        resOptionFour.getOrder() == 3
        resOptionFour.isCorrect()
        def resOptionFive = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK4.getSequence()}).findAny().orElse(null)
        resOptionFive.getContent() == OPTION_4_CONTENT
        resOptionFive.getOrder() == 5
        resOptionFive.isCorrect()
    }

    def "cannot update question, if order repeats"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: '1 option changed'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK1)
        optionDto.setOrder(1)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK2)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK3)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK4)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INCORRECT_ORDER
    }   

    def "cannot update question, if order not complete"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: '1 option changed'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK1)
        optionDto.setOrder(5)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK2)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK3)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK4)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INCORRECT_ORDER
    }   

    def "update question, delete last element of the order"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setNumberOfCorrect(3)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: '1 option removed'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK1)
        optionDto.setCorrect(false)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK2)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK3)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK4)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        and: 'a count to load options to memory due to in memory databse flaw'
        optionRepository.count()

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        result.getNumberOfCorrect() == 3
        result.getDifficulty() == 60
        and: 'are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getNumberOfAnswers() == 5
        result.getImage() != null
        and: 'an option is changed'
        result.getQuestionDetails().getOptions().size() == 5
        def resOptionOne = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK.getSequence()}).findAny().orElse(null)
        resOptionOne.getContent() == OPTION_1_CONTENT
        resOptionOne.getOrder() == 1
        resOptionOne.isCorrect()
        def resOptionTwo = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK2.getSequence()}).findAny().orElse(null)
        resOptionTwo.getContent() == OPTION_3_CONTENT
        resOptionTwo.getOrder() == 2
        resOptionTwo.isCorrect()
        def resOptionThree = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK3.getSequence()}).findAny().orElse(null)
        resOptionThree.getContent() == OPTION_4_CONTENT
        resOptionThree.getOrder() == 3
        resOptionThree.isCorrect()
        def resOptionFour = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK1.getSequence()}).findAny().orElse(null)
        resOptionFour.getContent() == OPTION_2_CONTENT
        !resOptionFour.isCorrect()
        def resOptionFive = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK4.getSequence()}).findAny().orElse(null)
        resOptionFive.getContent() == OPTION_4_CONTENT
        !resOptionFive.isCorrect()
    }   

    def "cannot update question, delete middle element of the order"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setNumberOfAnswers(4)
        questionDto.setNumberOfCorrect(3)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: '1 option removed'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK1)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK2)
        options.add(optionDto)
        optionDto.setCorrect(false)
        optionDto = new OptionDto(optionOK3)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK4)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "error"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INCORRECT_ORDER
    }   

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
