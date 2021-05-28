<template>
  <div class="question-answer">
    <v-row>
      <v-textarea
        v-model="sQuestionDetails.teacherAnswer"
        label="Answer"
        :rules="[(v) => !!v || 'Teacher answer content is required']"
        auto-grow
        required
        data-cy="questionTeacherAnswerTextArea"
        rows="4"
      ></v-textarea>
    </v-row>
  </div>
</template>

<script lang="ts">
import { Component, PropSync, Vue } from 'vue-property-decorator';

import OpenQuestionDetails from '@/models/management/questions/OpenQuestionDetails';

@Component
export default class OpenCreate extends Vue {
  @PropSync('questionDetails', { type: OpenQuestionDetails })
  sQuestionDetails!: OpenQuestionDetails;
}
</script>
