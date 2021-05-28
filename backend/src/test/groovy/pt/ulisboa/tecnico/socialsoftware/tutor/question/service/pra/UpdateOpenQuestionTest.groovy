package pt.ulisboa.tecnico.socialsoftware.tutor.question.service.pra

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class UpdateOpenQuestionTest extends SpockTest {
    def question
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

        question.setImage(image)
        def questionDetails = new OpenQuestion()
        questionDetails.setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_1)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)


    }

    def "update a question"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new OpenQuestionDto())

        and: 'updated teacher answer'
        questionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_2)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        and: 'are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getImage() != null
        and: 'the teacher answer is changed'
        result.getQuestionDetailsDto().getTeacherAnswer() == OPEN_QUESTION_TEACHER_ANSWER_2

    }

    def "update open question with invalid teacher answer"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new OpenQuestionDto())

        and: 'updated teacher answer with an invalid answer'
        questionDto.getQuestionDetailsDto().setTeacherAnswer(invalidString)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question throws an exception"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.INVALID_TEACHER_ANSWER

        where:
        invalidString << ["", "    ", null]

    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
