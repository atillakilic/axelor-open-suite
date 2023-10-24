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
package com.axelor.apps.cash.management.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.cash.management.db.CashManagement;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class CashManagementRepo extends CashManagementRepository {

  @Override
  public CashManagement save(CashManagement entity) {
	    try {
	    	Company company  = Beans.get(CompanyRepository.class).all().fetchOne();
	        if (entity.getCashManagementSeq() == null) {
	          String seq =
	              Beans.get(SequenceService.class)
	                  .getSequenceNumber(
	                      SequenceRepository.CASH_MANAGEMENT_SEQUENCE,
	                      company,
	                      CashManagement.class,
	                      "cashManagementSeq");
	          if (seq == null) {
	            throw new AxelorException(
	                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
	                I18n.get("There is no sequence set for the Cash Management for the company"),
	                company.getName());
	          }
	          entity.setCashManagementSeq(seq);
	        }
	        return super.save(entity);
	      } catch (Exception e) {
	        TraceBackService.traceExceptionFromSaveMethod(e);
	        throw new PersistenceException(e.getMessage(), e);
	      }
	    }
}
