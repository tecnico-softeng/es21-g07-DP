package pt.ulisboa.tecnico.socialsoftware.tutor.question.service.pem

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class UpdatePemTest extends SpockTest {
    def question
    def optionOK
    def optionKO
    def optionOK2
    def optionOK3
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
        question.setNumberOfAnswers(4)
        question.setNumberOfCorrect(3)
        question.setImage(image)
        def questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'three options'
        optionOK = new Option()
        optionOK.setContent(OPTION_1_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        optionOK.setQuestionDetails(questionDetails)
        optionRepository.save(optionOK)

        optionKO = new Option()
        optionKO.setContent(OPTION_2_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setSequence(1)
        optionKO.setQuestionDetails(questionDetails)
        optionRepository.save(optionKO)

        optionOK2 = new Option()
        optionOK2.setContent(OPTION_3_CONTENT)
        optionOK2.setCorrect(true)
        optionOK2.setSequence(2)
        optionOK2.setQuestionDetails(questionDetails)
        optionRepository.save(optionOK2)

        optionOK3 = new Option()
        optionOK3.setContent(OPTION_4_CONTENT)
        optionOK3.setCorrect(true)
        optionOK3.setSequence(3)
        optionOK3.setQuestionDetails(questionDetails)
        optionRepository.save(optionOK3)
    }

    def "update question, decrease number of correct answers to 2"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setNumberOfCorrect(2)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: '1 option changed'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        optionDto.setCorrect(false)
        options.add(optionDto)
        optionDto = new OptionDto(optionKO)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK2)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK3)
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
        result.getNumberOfCorrect() == 2
        result.getDifficulty() == 50
        and: 'are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getNumberOfAnswers() == 4
        result.getImage() != null
        and: 'an option is changed'
        result.getQuestionDetails().getOptions().size() == 4
        def resOptionOne = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK.getSequence()}).findAny().orElse(null)
        resOptionOne.getContent() == OPTION_1_CONTENT
        !resOptionOne.isCorrect()
        def resOptionTwo = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionKO.getSequence()}).findAny().orElse(null)
        resOptionTwo.getContent() == OPTION_2_CONTENT
        !resOptionTwo.isCorrect()
        def resOptionThree = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK2.getSequence()}).findAny().orElse(null)
        resOptionThree.getContent() == OPTION_3_CONTENT
        resOptionThree.isCorrect()
        def resOptionFour = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK3.getSequence()}).findAny().orElse(null)
        resOptionFour.getContent() == OPTION_4_CONTENT
        resOptionFour.isCorrect()
    }

    def "update question, increase number of correct answers to 4"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setNumberOfCorrect(4)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: '1 option changed'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        options.add(optionDto)
        optionDto = new OptionDto(optionKO)
        optionDto.setCorrect(true)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK2)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK3)
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
        result.getDifficulty() == 100
        and: 'are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getNumberOfAnswers() == 4
        result.getImage() != null
        and: 'an option is changed'
        result.getQuestionDetails().getOptions().size() == 4
        def resOptionOne = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK.getSequence()}).findAny().orElse(null)
        resOptionOne.getContent() == OPTION_1_CONTENT
        resOptionOne.isCorrect()
        def resOptionTwo = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionKO.getSequence()}).findAny().orElse(null)
        resOptionTwo.getContent() == OPTION_2_CONTENT
        resOptionTwo.isCorrect()
        def resOptionThree = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK2.getSequence()}).findAny().orElse(null)
        resOptionThree.getContent() == OPTION_3_CONTENT
        resOptionThree.isCorrect()
        def resOptionFour = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getSequence() == optionOK3.getSequence()}).findAny().orElse(null)
        resOptionFour.getContent() == OPTION_4_CONTENT
        resOptionFour.isCorrect()
    }

    def "update question, decrease number of correct answers to 0, should give error"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setNumberOfCorrect(0)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: '3 option changed'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        optionDto.setCorrect(false)
        options.add(optionDto)
        optionDto = new OptionDto(optionKO)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK2)
        optionDto.setCorrect(false)
        options.add(optionDto)
        optionDto = new OptionDto(optionOK3)
        optionDto.setCorrect(false)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(question.getId(), questionDto)
        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.ONE_CORRECT_OPTION_NEEDED
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
