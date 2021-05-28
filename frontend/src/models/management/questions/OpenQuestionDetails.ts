import QuestionDetails from '@/models/management/questions/QuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class OpenQuestionDetails extends QuestionDetails {
  teacherAnswer: string = '';

  constructor(jsonObj?: OpenQuestionDetails) {
    super(QuestionTypes.Open);
    if (jsonObj) {
      this.teacherAnswer = '';
    }
  }

  setAsNew(): void {}
}
