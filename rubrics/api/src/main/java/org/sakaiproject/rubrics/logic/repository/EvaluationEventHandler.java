/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics.logic.repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.sakaiproject.rubrics.logic.exception.UnprocessableEntityException;
import org.sakaiproject.rubrics.logic.model.Criterion;
import org.sakaiproject.rubrics.logic.model.CriterionOutcome;
import org.sakaiproject.rubrics.logic.model.Evaluation;
import org.sakaiproject.rubrics.logic.model.Metadata;
import org.sakaiproject.rubrics.logic.model.Rubric;
import org.sakaiproject.rubrics.logic.model.ToolItemRubricAssociation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@RepositoryEventHandler(Evaluation.class)
@Slf4j
public class EvaluationEventHandler {

    @Autowired
    private ToolItemRubricAssociationRepository toolItemRubricAssociationRepository;

    @HandleBeforeSave
    @HandleBeforeCreate
    public void beforeEvaluationCreateOrUpdate(Evaluation evaluation) {

        ToolItemRubricAssociation toolItemRubricAssociation = evaluation.getToolItemRubricAssociation();

        if (toolItemRubricAssociation != null) {

            Rubric rubric = toolItemRubricAssociation.getRubric();
            List<Long> criterionIds = rubric.getCriterions().stream().map(Criterion::getId).collect(Collectors.toList());
            List<CriterionOutcome> criterionOutcomes = evaluation.getCriterionOutcomes();
            List<Long> criterionOutcomeReferenceCriterionIds = criterionOutcomes.stream().map(
                    CriterionOutcome::getCriterionId).collect(Collectors.toList());

            Collections.sort(criterionIds);
            Collections.sort(criterionOutcomeReferenceCriterionIds);

            // TODO - Improve the detail provided
            if (!criterionIds.equals(criterionOutcomeReferenceCriterionIds)) {
                throw new UnprocessableEntityException(
                        "Evaluation Criterion Outcomes do not match number and IDs of Rubric Criterions.");
            }
            if (evaluation.getMetadata() == null){
                Metadata metadata = new Metadata();
                metadata.setCreated(LocalDateTime.now());
                metadata.setModified(LocalDateTime.now());
                evaluation.setMetadata(metadata);
            }else{
                Metadata metadata = evaluation.getMetadata();
                metadata.setModified(LocalDateTime.now());
                evaluation.setMetadata(metadata);
            }
        }
    }
}
