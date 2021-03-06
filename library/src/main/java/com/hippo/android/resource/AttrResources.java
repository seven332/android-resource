/*
 * Copyright 2017 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.android.resource;

/*
 * Created by Hippo on 5/8/2017.
 */

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.v7.content.res.AppCompatResources;
import android.util.TypedValue;

/**
 * Reads resources for attribute identifier.
 */
public final class AttrResources {
  private AttrResources() {}

  /** Lock object used to protect access to {@link #tmpValue}. */
  private static final Object tmpValueLock = new Object();
  /** Single-item pool used to minimize TypedValue allocations. */
  private static TypedValue tmpValue = new TypedValue();

  /** Lock object used to protect initializing {@link #resourcesGetter}. */
  private static final Object resourcesGetterLock = new Object();
  /** Used to get ColorStateList and Drawable for {@code AppCompatResources} or different API. **/
  private static ResourcesGetter resourcesGetter;

  /**
   * Returns a TypedValue suitable for temporary use. The obtained TypedValue
   * should be released using {@link #releaseTempTypedValue(TypedValue)}.
   *
   * @return a typed value suitable for temporary use
   */
  private static TypedValue obtainTempTypedValue() {
    TypedValue tmpValue = null;
    synchronized (tmpValueLock) {
      if (AttrResources.tmpValue != null) {
        tmpValue = AttrResources.tmpValue;
        AttrResources.tmpValue = null;
      }
    }
    if (tmpValue == null) {
      return new TypedValue();
    }
    return tmpValue;
  }

  /**
   * Returns a TypedValue to the pool. After calling this method, the
   * specified TypedValue should no longer be accessed.
   *
   * @param value the typed value to return to the pool
   */
  private static void releaseTempTypedValue(TypedValue value) {
    synchronized (tmpValueLock) {
      if (tmpValue == null) {
        tmpValue = value;
      }
    }
  }

  /**
   * Resolve a attribute value for a particular attribute ID.
   *
   * @param context the context to resolve from
   * @param attrId the desired attribute identifier
   * @param value the value container
   * @throws Resources.NotFoundException if can't resolve the given ID
   */
  private static void resolveAttribute(Context context, int attrId, TypedValue value,
      boolean resolveRefs) throws Resources.NotFoundException {
    if (!context.getTheme().resolveAttribute(attrId, value, resolveRefs)) {
      throw new Resources.NotFoundException(
          "Can't resolve attribute ID #0x" + Integer.toHexString(attrId));
    }
  }

  /**
   * Resolve a boolean associated with a particular attribute ID. This can be
   * used with any integral resource value, and will return true if it is
   * non-zero.
   *
   * @param context the context to resolve from
   * @param id the desired attribute identifier
   * @return the boolean value
   * @throws Resources.NotFoundException if the given ID does not exist
   */
  public static boolean getAttrBoolean(@NonNull Context context, @AttrRes int id)
      throws Resources.NotFoundException {
    final TypedValue value = obtainTempTypedValue();
    try {
      resolveAttribute(context, id, value, true);
      if (value.type >= TypedValue.TYPE_FIRST_INT
          && value.type <= TypedValue.TYPE_LAST_INT) {
        return value.data != 0;
      }
      throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id)
          + " type #0x" + Integer.toHexString(value.type) + " is not valid");
    } finally {
      releaseTempTypedValue(value);
    }
  }

  /**
   * Resolve an integer associated with a particular attribute ID.
   *
   * @param context the context to resolve from
   * @param id the desired attribute identifier
   * @return the integer value
   * @throws Resources.NotFoundException if the given ID does not exist
   */
  public static int getAttrInteger(@NonNull Context context, @AttrRes int id)
      throws Resources.NotFoundException {
    final TypedValue value = obtainTempTypedValue();
    try {
      resolveAttribute(context, id, value, true);
      if (value.type >= TypedValue.TYPE_FIRST_INT
          && value.type <= TypedValue.TYPE_LAST_INT) {
        return value.data;
      }
      throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id)
          + " type #0x" + Integer.toHexString(value.type) + " is not valid");
    } finally {
      releaseTempTypedValue(value);
    }
  }

  /**
   * Resolve an floating-point associated with a particular attribute ID.
   *
   * @param context the context to resolve from
   * @param id the desired attribute identifier
   * @return the floating-point value
   * @throws Resources.NotFoundException if the given ID does not exist
   */
  public static float getAttrFloat(@NonNull Context context, @AttrRes int id)
      throws Resources.NotFoundException {
    final TypedValue value = obtainTempTypedValue();
    try {
      resolveAttribute(context, id, value, true);
      if (value.type == TypedValue.TYPE_FLOAT) {
        return value.getFloat();
      }
      throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id)
          + " type #0x" + Integer.toHexString(value.type) + " is not valid");
    } finally {
      releaseTempTypedValue(value);
    }
  }

  /**
   * Resolve a dimensional for a particular attribute ID. Unit
   * conversions are based on the current {@link android.util.DisplayMetrics}
   * associated with the content.
   *
   * @param context the context to resolve from
   * @param id the desired attribute identifier
   * @return resource dimension value multiplied by the appropriate
   * @throws Resources.NotFoundException if the given ID does not exist
   */
  public static float getAttrDimension(@NonNull Context context, @AttrRes int id)
      throws Resources.NotFoundException {
    final TypedValue value = obtainTempTypedValue();
    try {
      resolveAttribute(context, id, value, true);
      if (value.type == TypedValue.TYPE_DIMENSION) {
        return TypedValue.complexToDimension(
            value.data, context.getResources().getDisplayMetrics());
      }
      throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id)
          + " type #0x" + Integer.toHexString(value.type) + " is not valid");
    } finally {
      releaseTempTypedValue(value);
    }
  }

  /**
   * Resolve a dimensional for a particular attribute ID for use
   * as an offset in raw pixels. This is the same as
   * {@link #getAttrDimension}, except the returned value is converted to
   * integer pixels for you. An offset conversion involves simply
   * truncating the base value to an integer.
   *
   * @param context the context to resolve from
   * @param id the desired attribute identifier
   * @return resource dimension value multiplied by the appropriate
   *         metric and truncated to integer pixels
   * @throws Resources.NotFoundException if the given ID does not exist
   */
  public static int getAttrDimensionPixelOffset(@NonNull Context context, @AttrRes int id)
      throws Resources.NotFoundException {
    final TypedValue value = obtainTempTypedValue();
    try {
      resolveAttribute(context, id, value, true);
      if (value.type == TypedValue.TYPE_DIMENSION) {
        return TypedValue.complexToDimensionPixelOffset(
            value.data, context.getResources().getDisplayMetrics());
      }
      throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id)
          + " type #0x" + Integer.toHexString(value.type) + " is not valid");
    } finally {
      releaseTempTypedValue(value);
    }
  }

  /**
   * Resolve a dimensional for a particular attribute ID for use
   * as a size in raw pixels. This is the same as
   * {@link #getAttrDimension}, except the returned value is converted to
   * integer pixels for use as a size. A size conversion involves
   * rounding the base value, and ensuring that a non-zero base value
   * is at least one pixel in size.
   *
   * @param context the context to resolve from
   * @param id the desired attribute identifier
   * @return resource dimension value multiplied by the appropriate
   *         metric and truncated to integer pixels.
   * @throws Resources.NotFoundException if the given ID does not exist
   */
  public static int getAttrDimensionPixelSize(@NonNull Context context, @AttrRes int id)
      throws Resources.NotFoundException {
    final TypedValue value = obtainTempTypedValue();
    try {
      resolveAttribute(context, id, value, true);
      if (value.type == TypedValue.TYPE_DIMENSION) {
        return TypedValue.complexToDimensionPixelSize(
            value.data, context.getResources().getDisplayMetrics());
      }
      throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id)
          + " type #0x" + Integer.toHexString(value.type) + " is not valid");
    } finally {
      releaseTempTypedValue(value);
    }
  }

  @SuppressWarnings("ConstantConditions")
  private static boolean hasAppCompatResources() {
    try {
      return AppCompatResources.class != null;
    } catch (Throwable e) {
      return false;
    }
  }

  private static ResourcesGetter getResourcesGetter() {
    // Double check null
    if (resourcesGetter == null) {
      synchronized (resourcesGetterLock) {
        if (resourcesGetter == null) {
          if (hasAppCompatResources()) {
            resourcesGetter = new AppCompatResourcesGetter();
          } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            resourcesGetter = new LollipopResourcesGetter();
          } else {
            resourcesGetter = new BaseResourcesGetter();
          }
        }
      }
    }
    return resourcesGetter;
  }

  /**
   * Resolve a color integer associated with a particular attribute ID.
   * If the resource holds a complex {@link ColorStateList}, then the default
   * color from the set is returned.
   *
   * @param context the context to resolve from
   * @param id the desired attribute identifier
   * @return a single color value in the form 0xAARRGGBB
   * @throws Resources.NotFoundException if the given ID does not exist
   */
  public static int getAttrColor(@NonNull Context context, @AttrRes int id)
      throws Resources.NotFoundException {
    final TypedValue value = obtainTempTypedValue();
    try {
      resolveAttribute(context, id, value, false);
      if (value.type >= TypedValue.TYPE_FIRST_INT
          && value.type <= TypedValue.TYPE_LAST_INT) {
        return value.data;
      } else if (value.type == TypedValue.TYPE_REFERENCE) {
        ColorStateList colorStateList = getResourcesGetter().getColorStateList(context, value.data);
        if (colorStateList != null) {
          return colorStateList.getDefaultColor();
        }
      }
      throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id)
          + " type #0x" + Integer.toHexString(value.type) + " is not valid");
    } finally {
      releaseTempTypedValue(value);
    }
  }

  /**
   * Resolve a color state list associated with a particular attribute ID.
   * The resource may contain either a single raw color value or a
   * complex {@link ColorStateList} holding multiple possible colors.
   *
   * @param context the context to resolve from
   * @param id the desired attribute identifier
   * @return A themed ColorStateList object containing either a single solid
   *         color or multiple colors that can be selected based on a state.
   * @throws Resources.NotFoundException if the given ID does not exist
   */
  public static ColorStateList getAttrColorStateList(@NonNull Context context, @AttrRes int id)
      throws Resources.NotFoundException {
    final TypedValue value = obtainTempTypedValue();
    try {
      resolveAttribute(context, id, value, false);
      if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT
          && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
        return ColorStateList.valueOf(value.data);
      } else if (value.type == TypedValue.TYPE_REFERENCE) {
        return getResourcesGetter().getColorStateList(context, value.data);
      }
      throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id)
          + " type #0x" + Integer.toHexString(value.type) + " is not valid");
    } finally {
      releaseTempTypedValue(value);
    }
  }

  /**
   * Resolve a drawable object associated with a particular attribute ID.
   *
   * @param context the context to resolve from
   * @param id the desired attribute identifier
   * @return Drawable An object that can be used to draw this resource.
   * @throws Resources.NotFoundException if the given ID does not exist
   */
  public static Drawable getAttrDrawable(@NonNull Context context, @AttrRes int id)
      throws Resources.NotFoundException {
    final TypedValue value = obtainTempTypedValue();
    try {
      resolveAttribute(context, id, value, false);
      if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT
          && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
        return new ColorDrawable(value.data);
      } else if (value.type == TypedValue.TYPE_REFERENCE) {
        return getResourcesGetter().getDrawable(context, value.data);
      }
      throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id)
          + " type #0x" + Integer.toHexString(value.type) + " is not valid");
    } finally {
      releaseTempTypedValue(value);
    }
  }
}
