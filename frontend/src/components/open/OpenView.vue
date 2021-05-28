<template>
  <span>{{ questionDetails.teacherAnswer }}</span>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import OpenQuestionDetails from '@/models/management/questions/OpenQuestionDetails';

@Component
export default class OpenView extends Vue {
  @Prop() readonly questionDetails!: OpenQuestionDetails;
}
</script>
