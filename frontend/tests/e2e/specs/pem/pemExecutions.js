describe('Manage PEM', () => {

    function orderByTitle() {
        cy.contains("Title").click();
    }

    function orderByTitleTwice() {
        orderByTitle();
        orderByTitle();
    }

    function validateNotOrderingQuestion(
        title,
        content,
        optionPrefix,
        correctOptions = []
    ) {
        cy.get('[data-cy="showQuestionDialog"]')
            .should('be.visible')
            .within(($ls) => {
                cy.get('.headline').should('contain', title);
                cy.get('span > p').should('contain', content);
                cy.get('li').each(($el, index, $list) => {
                cy.get($el).should('contain', optionPrefix + (index+1));
                if (correctOptions.includes(index)) {
                    cy.get($el).should('contain', '[★]');
                } else {
                    cy.get($el).should('not.contain', '[★]');
                }
            });
        });
    }
    
    function validateNotOrderingQuestionFull(
        title,
        content,
        optionPrefix,
        correctOptions = [],
    ) {
        orderByTitle();
        cy.log('Validate question with show dialog. ' + correctOptions);
        cy.get('[data-cy="questionTitleGrid"]').first().click();
        validateNotOrderingQuestion(title, content, optionPrefix, correctOptions);
        cy.get('button').contains('close').click();
    }

    function validateOrderingQuestion(
        title,
        content,
        optionPrefix,
        correctOptions = [],
        order = []
    ) {
        cy.get('[data-cy="showQuestionDialog"]')
            .should('be.visible')
            .within(($ls) => {
                cy.get('.headline').should('contain', title);
                cy.get('span > p').should('contain', content);
                cy.get('li').each(($el, index, $list) => {
                cy.get($el).should('contain', optionPrefix + (index+1));
                if (correctOptions.includes(index)) {
                    cy.get($el).should('contain', '[★]');
                    cy.get($el).should('contain', '[#' + order[correctOptions.indexOf(index)] + ']');
                } else {
                    cy.get($el).should('not.contain', '[★]');
                    cy.get($el).should('not.contain', '[#');
                }
            });
        });
    }
    
    function validateOrderingQuestionFull(
        title,
        content,
        optionPrefix,
        correctOptions = [],
        order = []
    ) {
        orderByTitleTwice();
        cy.log('Validate question with show dialog. ' + correctOptions);
        cy.get('[data-cy="questionTitleGrid"]').first().click();
        validateOrderingQuestion(title, content, optionPrefix, correctOptions, order);
        cy.get('button').contains('close').click();
    }

    before(() => {
        cy.cleanMultipleChoiceQuestionsByName('Title No Order PEM');
        cy.cleanMultipleChoiceQuestionsByName('Title Order PEM');
        cy.cleanMultipleChoiceQuestionsByName('Title Order PEM - Edited');
        cy.cleanMultipleChoiceQuestionsByName('Title Order PEM - Edited - DUP');
    });

    after(() => {
      cy.cleanMultipleChoiceQuestionsByName('Title No Order PEM');
      cy.cleanMultipleChoiceQuestionsByName('Title Order PEM');
      cy.cleanMultipleChoiceQuestionsByName('Title Order PEM - Edited');
      cy.cleanMultipleChoiceQuestionsByName('Title Order PEM - Edited - DUP');
    });

    beforeEach(() => {
        cy.demoTeacherLogin();
        cy.server();
        cy.route('GET', '/courses/*/questions').as('getQuestions');
        cy.route('GET', '/courses/*/topics').as('getTopics');
        cy.get('[data-cy="managementMenuButton"]').click();
        cy.get('[data-cy="questionsTeacherMenuButton"]').click();

        cy.wait('@getQuestions').its('status').should('eq', 200);
        cy.wait('@getTopics').its('status').should('eq', 200);
    });

    afterEach(() => {
        cy.logout();
    });
      
    it('Teacher creates a new question without order', function () {
        cy.get('button').contains('New Question').click();

        cy.get('[data-cy="createOrEditQuestionDialog"]').parent().should('be.visible');
        cy.get('span.headline').should('contain', 'New Question');

        cy.get('[data-cy="questionTitleTextArea"]').type('Title No Order PEM', { force: true });
        cy.get('[data-cy="questionQuestionTextArea"]').type('Question PEM', { force: true });

        cy.get('[data-cy="questionOptionsInput"')
        .should('have.length', 4)
        .each(($el, index, $list) => {
            cy.get($el).within(($ls) => {
                if (index < 3) {
                    cy.get(`[data-cy="Switch${index + 1}"]`).check({ force: true });
                }
                cy.get(`[data-cy="Option${index + 1}"]`).type('Option ' + (index+1));
            });
        });

        cy.route('POST', '/courses/*/questions/').as('postQuestion');
        cy.get('button').contains('Save').click();

        cy.wait('@postQuestion').its('status').should('eq', 200);

        cy.get('[data-cy="questionTitleGrid"]').first().should('contain', 'Title No Order PEM');

        validateNotOrderingQuestionFull('Title No Order PEM', 'Question PEM', 'Option ', [0,1,2]);
    });
    
    it('Teacher creates a new question with order', function () {
        cy.get('button').contains('New Question').click();

        cy.get('[data-cy="createOrEditQuestionDialog"]').parent().should('be.visible');
        cy.get('span.headline').should('contain', 'New Question');

        cy.get('[data-cy="questionTitleTextArea"]').type('Title Order PEM', { force: true });
        cy.get('[data-cy="questionQuestionTextArea"]').type('Question PEM', { force: true });

        cy.get('[data-cy="questionOptionsInput"')
        .should('have.length', 4)
        .each(($el, index, $list) => {
            cy.get($el).within(($ls) => {
                if (index < 3) {
                    cy.get(`[data-cy="Switch${index + 1}"]`).check({ force: true });
                }
                cy.get(`[data-cy="Option${index + 1}"]`).type('Option ' + (index+1));
            });
        });

        cy.get('[data-cy="questionOptionsInput"')
        .should('have.length', 4)
        .each(($el, index, $list) => {
            cy.get($el).within(($ls) => {
                if (index < 3) {
                    cy.get(`[data-cy="Order${index + 1}"]`).type(index+1);
                }
            });
        });

        cy.route('POST', '/courses/*/questions/').as('postQuestion');
        cy.get('button').contains('Save').click();

        cy.wait('@postQuestion').its('status').should('eq', 200);

        cy.get('[data-cy="questionTitleGrid"]').first().should('contain', 'Title Order PEM');

        validateOrderingQuestionFull('Title Order PEM', 'Question PEM', 'Option ', [0,1,2], [1,2,3]);
    });
    
    it('Can view question with order (with button)', function () {
        orderByTitleTwice();
        cy.get('tbody tr').first().within(($list) => {
            cy.get('button').contains('visibility').click();
        });
    
        validateOrderingQuestion('Title Order PEM', 'Question PEM', 'Option ', [0,1,2], [1,2,3]);
        cy.get('button').contains('close').click();
    });
    
    it('Can view question with order (with click)', function () {
        orderByTitleTwice();
        cy.get('[data-cy="questionTitleGrid"]').first().click();
    
        validateOrderingQuestion('Title Order PEM', 'Question PEM', 'Option ', [0,1,2], [1,2,3]);
        cy.get('button').contains('close').click();
    });
    
    it('Can update title (with right-click)', function () {
        orderByTitleTwice();
        cy.route('PUT', '/questions/*').as('updateQuestion');
    
        cy.get('[data-cy="questionTitleGrid"]').first().rightclick();
    
        cy.get('[data-cy="createOrEditQuestionDialog"]')
          .parent()
          .should('be.visible')
          .within(($list) => {
            cy.get('span.headline').should('contain', 'Edit Question');
    
            cy.get('[data-cy="questionTitleTextArea"]')
              .clear({ force: true })
              .type('Title Order PEM - Edited', { force: true });
    
            cy.get('button').contains('Save').click();
          });
    
        cy.wait('@updateQuestion').its('status').should('eq', 200);
    
        cy.get('[data-cy="questionTitleGrid"]')
          .first()
          .should('contain', 'Title Order PEM - Edited');
    
        validateOrderingQuestionFull('Title Order PEM - Edited', 'Question PEM', 'Option ', [0,1,2], [1,2,3]);
      });
      
      it('Can update content (with button)', function () {
        orderByTitleTwice();
        cy.route('PUT', '/questions/*').as('updateQuestion');
    
        cy.get('tbody tr')
          .first()
          .within(($list) => {
            cy.get('button').contains('edit').click();
          });
    
        cy.get('[data-cy="createOrEditQuestionDialog"]')
          .parent()
          .should('be.visible')
          .within(($list) => {
            cy.get('span.headline').should('contain', 'Edit Question');
    
            cy.get('[data-cy="questionQuestionTextArea"]')
              .clear({ force: true })
              .type('Question PEM - Edited', { force: true });
    
            cy.get('button').contains('Save').click();
          });
    
        cy.wait('@updateQuestion').its('status').should('eq', 200);
    
        validateOrderingQuestionFull('Title Order PEM - Edited', 'Question PEM - Edited', 'Option ', [0,1,2], [1,2,3]);
      });
      
      it('Can duplicate question', function () {
        orderByTitleTwice();
        cy.get('tbody tr')
          .first()
          .within(($list) => {
            cy.get('button').contains('cached').click();
          });
    
        cy.get('[data-cy="createOrEditQuestionDialog"]')
          .parent()
          .should('be.visible');
    
        cy.get('span.headline').should('contain', 'New Question');
    
        cy.get('[data-cy="questionTitleTextArea"]')
          .should('have.value', 'Title Order PEM - Edited')
          .type('{end} - DUP', { force: true });
        cy.get('[data-cy="questionQuestionTextArea"]').should(
          'have.value',
          'Question PEM - Edited'
        );
    
        cy.get('[data-cy="questionOptionsInput"')
          .should('have.length', 4)
          .each(($el, index, $list) => {
            cy.get($el).within(($ls) => {
              cy.get('textarea').should('have.value', 'Option ' + (index+1));
            });
          });
    
        cy.route('POST', '/courses/*/questions/').as('postQuestion');
    
        cy.get('button').contains('Save').click();
    
        cy.wait('@postQuestion').its('status').should('eq', 200);
    
        cy.get('[data-cy="questionTitleGrid"]')
          .first()
          .should('contain', 'Title Order PEM - Edited - DUP');
    
          validateOrderingQuestionFull('Title Order PEM - Edited - DUP', 'Question PEM - Edited', 'Option ', [0,1,2], [1,2,3]);
      });
      
      it('Can delete created question', function () {
        cy.route('DELETE', '/questions/*').as('deleteQuestion');
        cy.get('tbody tr')
          .first()
          .within(($list) => {
            cy.get('button').contains('delete').click();
          });
    
        cy.wait('@deleteQuestion').its('status').should('eq', 200);
      });
});