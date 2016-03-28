/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.integration.dataservices.validation;

import java.math.BigDecimal;
import java.util.Date;
import javax.validation.Valid;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import org.ocelotds.integration.objects.WithConstraint;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class ValidationCdiDataService {
	
	public void methodWithValidationArguments(@NotNull String str0, @NotNull String str1, @NotNull String str2) {}
	
	public void methodWithArgumentNotNull(@NotNull String str0) {}

	public void methodWithArgumentNull(@Null String str0) {}

	public void methodWithArgumentMax(@Max(10) int int0) {}

	public void methodWithArgumentMin(@Min(10) int int0) {}

	public void methodWithArgumentFuture(@Future Date date0) {}

	public void methodWithArgumentPast(@Past Date date0) {}

	public void methodWithArgumentFalse(@AssertFalse Boolean bool0) {}

	public void methodWithArgumentTrue(@AssertTrue Boolean bool0) {}

	public void methodWithArgumentDecimalMax(@DecimalMax("50") long lg0) {}

	public void methodWithArgumentDecimalMin(@DecimalMin("50") long lg0) {}

	public void methodWithArgumentDigits(@Digits(integer = 3, fraction = 2) BigDecimal bd0) {}

	public void methodWithArgumentSize2_10(@Size(min = 2, max = 10) String str0) {}

	public void methodWithArgumentPattern(@Pattern(regexp = "\\d*") String str0) {}
	
	public void methodWithArgumentConstraint(@Valid WithConstraint wc) {}
}
