/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.purchase.db.repo;

import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.purchase.db.PurchaseRequestRu;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class PurchaseRequestManagementRepositoryRu extends PurchaseRequestRuRepository {

  @Override
  public PurchaseRequestRu save(PurchaseRequestRu entity) {
    try {
      if (entity.getPurchaseRequestSeq() == null) {
        String seq =
            Beans.get(SequenceService.class)
                .getSequenceNumber(
                    SequenceRepository.PURCHASE_REQUEST_RU,
                    entity.getCompany(),
                    PurchaseRequestRu.class,
                    "purchaseRequestSeq");
        if (seq == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PurchaseExceptionMessage.PURCHASE_REQUEST_1),
              entity.getCompany().getName());
        }
        entity.setPurchaseRequestSeq(seq);
      }
      return super.save(entity);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}