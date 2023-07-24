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

import com.axelor.apps.hr.db.CompanyExpencesTypeLineRu;
import com.axelor.apps.hr.db.CompanyExpencesTypeRu;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class CompanyExpenceTypeRuController {

  public void calculateTotalPrise(ActionRequest request, ActionResponse response) {
    try {

      CompanyExpencesTypeRu companyExpencesType =
          request.getContext().asType(CompanyExpencesTypeRu.class);

      BigDecimal totalPrise = new BigDecimal(0);

      for (CompanyExpencesTypeLineRu companyExpencesTypeLine :
          companyExpencesType.getCompanyExpenceTypeLine()) {

        if (companyExpencesTypeLine.getTypeStatus()) {
          totalPrise = totalPrise.add(companyExpencesTypeLine.getExpencePrice());
        }
      }
      response.setValue("totalPrise", totalPrise);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
