package com.oridev.variantsstubsgenerator.sample.utils;

import android.content.Context;
import android.support.annotation.Nullable;

import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;
import com.oridev.variantsstubsgenerator.sample.R;
import com.oridev.variantsstubsgenerator.sample.utils.premium.PaidPrivateFunctionality;

/**
 * Created by Ori on 18/08/2016.
 */
@RequiresVariantStub(
// flavorTo is the flavor we want stubs to be generated to.
flavorTo = "free")
public class PaidFunctionality {

    public static @Nullable String getPaidMessage(Context context) {
        return context.getString(R.string.message_orange_success);
    }

	/* Additional examples of library usage... */

	// For each public method in annotated class, a stub method will be
	// generated
	public static int publicMethod(int x, float y) {
		// Return a random number.
		// In the generated stub the return value will be the default for number
		// primitives - 0
		return Double.valueOf(Math.random()).intValue();
	}

	// For private methods stubs are not necessary
	private static void privateMethod(int x) {
		// added for testing removing unused imports
		PaidPrivateFunctionality.someMethod();
	}

	// Stubs will also be generated for inner classes
	public class InnerClass {

	}

	// Support for generic types
	public interface GenericInnerInterface<T> {

		/**
		 * Listener for operation progress update.
		 *
		 * @param progress
		 *            The current progress.
		 * @param max
		 *            The total amount.
		 */
		void onProgressUpdate(Long progress, Long max, Object extraData);

		/**
		 * Listener for operation finished successfully.
		 *
		 * @param response
		 *            The response.
		 */
		void onFinish(T response);
	}

	// The stubs for enums will contain the enum constants and public methods
	public enum EnumType {
		ENUM_VALUE_1, ENUM_VALUE_2;

		public boolean isValue1() {
			switch (this) {
				case ENUM_VALUE_1 :
					return true;
				default :
					return false;
			}
		}
	}

}
