import AnswerDetails from '@/models/management/questions/AnswerDetails';
import { QuestionTypes, convertToLetter } from '@/services/QuestionHelpers';
import ItemConnectionQuestionDetails from '@/models/management/questions/pci/ItemConnectionQuestionDetails';

export default class ItemConnectionAnswerDetails extends AnswerDetails {

  constructor(jsonObj?: ItemConnectionAnswerDetails) {
    super(QuestionTypes.ItemConnection);
    //if (jsonObj) {
    //  this.orderedSlots = jsonObj.orderedSlots.map(
     //   (option: CodeOrderAnswerOrderedSlot) =>
     //     new CodeOrderAnswerOrderedSlot(option)
    //  );
    //}
  }

  isCorrect(questionDetails: ItemConnectionQuestionDetails): boolean {
    return true;
    //return (
    //  this.orderedSlots.length ===
    //    questionDetails.codeOrderSlots.filter(os => os.order != null).length &&
     // this.orderedSlots.filter(os => !os.correct).length == 0
    //);
  }

  answerRepresentation(questionDetails: ItemConnectionQuestionDetails): string {
    return '';
    //return (
    //  this.orderedSlots.map(x => '' + ((x.sequence || 0) + 1)).join(' | ') ||
     // '-'
    //);
  }
}
