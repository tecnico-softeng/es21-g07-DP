import { QuestionTypes, convertToLetter } from '@/services/QuestionHelpers';

export default class ConnectionLink {
  id: number | null = null;
  content: string = '';

  constructor(jsonObj?: ConnectionLink) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.content = '';
    }
  }
}
