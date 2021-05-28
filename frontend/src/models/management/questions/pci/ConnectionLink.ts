import { QuestionTypes, convertToLetter } from '@/services/QuestionHelpers';

export default class ConnectionLink {
  id: number | null = null;
  origin: number = -1;
  destiny: number = -1;

  constructor(jsonObj?: ConnectionLink) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.origin = jsonObj.origin;
      this.destiny = jsonObj.destiny;
    }
  }

  setOrigin(o: number){
    this.origin = o;
  }

  setDestiny(d: number){
    this.destiny = d;
  }
}
