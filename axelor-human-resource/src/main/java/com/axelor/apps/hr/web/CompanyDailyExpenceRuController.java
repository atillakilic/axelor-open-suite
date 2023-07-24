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
package com.axelor.apps.hr.web;

import com.axelor.apps.hr.db.CompanyDailyExpencesLineRu;
import com.axelor.apps.hr.db.CompanyDailyExpencesRu;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class CompanyDailyExpenceRuController {

  public void calculateSubTotal(ActionRequest request, ActionResponse response) {
    try {

      CompanyDailyExpencesRu companyExpences =
          request.getContext().asType(CompanyDailyExpencesRu.class);

      BigDecimal totalSubCost = new BigDecimal(0);

      for (CompanyDailyExpencesLineRu companyExpencesLine :
          companyExpences.getCompanyDailyExpencesLine()) {
        totalSubCost = totalSubCost.add(companyExpencesLine.getTotalCost());
      }
      response.setValue("totalSubCost", totalSubCost);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
