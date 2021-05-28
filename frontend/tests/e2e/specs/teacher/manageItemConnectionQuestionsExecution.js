describe('Manage Item Connection Questions Walk-through', () => {
    function validateQuestion(title, content) {
     connections = [[0,1],[1,0]];
     groupRight = ["Edit_Right_0", "Edit_Right_1"]
     groupLeft = ["Edit_Left_0", "Edit_Left_1"]
      cy.get('[data-cy="showQuestionDialog"]')
        .should('be.visible')
        .within(($ls) => {
          cy.get('.headline').should('contain', title);
          cy.get('span > p').should('contain', content);
          cy.get('[data-cy="row"]').each(($el, index, $list) => {
            cy.get($el).should('contain', connections[index][0]+' -> '+connections[index][1]);
          });
          cy.get('[data-cy="groupLeft"]').each(($el, index, $list) => {
            cy.get($el).should('contain', '-> '+groupLeft[index]);
          });
          cy.get('[data-cy="groupRight"]').each(($el, index, $list) => {
            cy.get($el).should('contain', '-> '+groupRight[index]);
          });
        });
    }
  
    function validateQuestionFull(title, content) {
      cy.log('Validate question with show dialog.');
  
      cy.get('[data-cy="questionTitleGrid"]').first().click();
  
      validateQuestion(title, content);
  
      cy.get('button').contains('close').click();
    }

    function editOption(index, content){
      var data = content + index
      cy.get('[data-cy='+data+']')
      .click({ force: true });

      cy.get('[data-cy="Edit"]')
        .type(data, { force: true })

      cy.get('[data-cy="Save"]')
      .click({ force: true });
    }

    function setConnection(o, d){
      cy.get('[data-cy="Click_Left_'+o+'"]')
      .click({ force: true });
      cy.get('[data-cy="Click_Right_'+d+'"]')
      .click({ force: true });
    }
  
    before(() => {
      cy.cleanItemConnectionQuestionsByName('Cypress Question Example - 01 - Edited');
    });
    after(() => {
      cy.cleanItemConnectionQuestionsByName('Cypress Question Example - 01 - Edited');
    });
  
    beforeEach(() => {
      cy.demoTeacherLogin();
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
  
    it('Creates an item connection question', function () {
      cy.get('button').contains('New Question').click();
  
      cy.get('[data-cy="createOrEditQuestionDialog"]')
        .parent()
        .should('be.visible');
  
      cy.get('span.headline').should('contain', 'New Question');
  
      cy.get(
        '[data-cy="questionTitleTextArea"]'
      ).type('Cypress Question Example - 01', { force: true });
      cy.get('[data-cy="questionQuestionTextArea"]').type(
        'Cypress Question Example - Content - 01',
        {
          force: true,
        }
      );
  
      cy.get('[data-cy="questionTypeInput"]')
        .type('item_connection', { force: true })
        .click({ force: true });
  
      cy.wait(1000);

      editOption(0, "Edit_Left_");
      editOption(1, "Edit_Left_");
      editOption(0, "Edit_Right_");
      editOption(1, "Edit_Right_");
    
      setConnection(0,1);
      setConnection(1,0);


      cy.route('POST', '/courses/*/questions/').as('postQuestion');
  
      cy.get('button').contains('Save').click();
        
      cy.wait('@postQuestion').its('status').should('eq', 200);
  
      cy.get('[data-cy="questionTitleGrid"]')
        .first()
        .should('contain', 'Cypress Question Example - 01');
  
      validateQuestionFull(
        'Cypress Question Example - 01',
        'Cypress Question Example - Content - 01'
      );
    });

    it('Can update title (with right-click)', function () {
      cy.route('PUT', '/questions/*').as('updateQuestion');
  
      cy.get('[data-cy="questionTitleGrid"]').first().rightclick();
  
      cy.wait(1000); //making sure codemirror loaded
  
      cy.get('[data-cy="createOrEditQuestionDialog"]')
        .parent()
        .should('be.visible')
        .within(($list) => {
          cy.get('span.headline').should('contain', 'Edit Question');
  
          cy.get('[data-cy="questionTitleTextArea"]')
            .clear({ force: true })
            .type('Cypress Question Example - 01 - Edited', { force: true });
  
          cy.get('button').contains('Save').click();
        });
  
      cy.wait('@updateQuestion').its('status').should('eq', 200);
  
      cy.get('[data-cy="questionTitleGrid"]')
        .first()
        .should('contain', 'Cypress Question Example - 01 - Edited');
  
      validateQuestionFull(
        (title = 'Cypress Question Example - 01 - Edited'),
        (content = 'Cypress Question Example - Content - 01')
      );
    });

    it('Can update content (with button)', function () {
      cy.route('PUT', '/questions/*').as('updateQuestion');
  
      cy.get('tbody tr')
        .first()
        .within(($list) => {
          cy.get('button').contains('edit').click();
        });
  
      cy.wait(1000); //making sure codemirror loaded
  
      cy.get('[data-cy="createOrEditQuestionDialog"]')
        .parent()
        .should('be.visible')
        .within(($list) => {
          cy.get('span.headline').should('contain', 'Edit Question');
  
          cy.get('[data-cy="questionQuestionTextArea"]')
            .clear({ force: true })
            .type('Cypress New Content For Question!', { force: true });
  
          cy.get('button').contains('Save').click();
        });
  
      cy.wait('@updateQuestion').its('status').should('eq', 200);
  
      validateQuestionFull(
        (title = 'Cypress Question Example - 01 - Edited'),
        (content = 'Cypress New Content For Question!')
      );
    });

    it('Can view question (with button)', function () {
        cy.get('tbody tr')
            .first()
            .within(($list) => {
                cy.get('button').contains('visibility').click();
            });

        cy.wait(1000);

        validateQuestion(
            'Cypress Question Example - 01 - Edited',
            'Cypress New Content For Question!'
        );

        cy.get('button').contains('close').click();
    });

    it('Can view question (with click)', function () {
        cy.get('[data-cy="questionTitleGrid"]').first().click();

        cy.wait(1000); //making sure codemirror loaded

        validateQuestion(
          'Cypress Question Example - 01 - Edited',
          'Cypress New Content For Question!'
        );

        cy.get('button').contains('close').click();
    });

    it('Can duplicate question', function () {
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
        .should('have.value', 'Cypress Question Example - 01 - Edited')
        .type('{end} - DUP', { force: true });
      cy.get('[data-cy="questionQuestionTextArea"]').should(
        'have.value',
        'Cypress New Content For Question!'
      );
  
  
      cy.route('POST', '/courses/*/questions/').as('postQuestion');
  
      cy.get('button').contains('Save').click();
  
      cy.wait('@postQuestion').its('status').should('eq', 200);
  
      cy.get('[data-cy="questionTitleGrid"]')
        .first()
        .should('contain', 'Cypress Question Example - 01 - Edited - DUP');
  
      validateQuestionFull(
        'Cypress Question Example - 01 - Edited - DUP',
        'Cypress New Content For Question!'
      );
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
})