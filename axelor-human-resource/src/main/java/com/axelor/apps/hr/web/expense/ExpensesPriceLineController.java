/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
/*
< * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.web.expense;

import com.axelor.apps.base.db.ExpencesTypePriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.ExpensePriceLine;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class ExpensesPriceLineController {

  public void setExpenseTypeAmount(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ExpensePriceLine expensePriceLine = request.getContext().asType(ExpensePriceLine.class);
    Product product = expensePriceLine.getExpenseProduct();

    if (product == null) {
      response.setValue("amount", 00);
      return;
    }
    List<ExpencesTypePriceList> expencePriceList = product.getExpenceTypePriceList();
    if (expencePriceList.size() == 0) {
      response.setValue("amount", 00);
      return;
    }
    for (ExpencesTypePriceList expencesTypePrice : expencePriceList) {
      if (expencesTypePrice.getIsActive()) {
        response.setValue("amount", expencesTypePrice.getPrice());
        return;
      }
    }
  }
}
