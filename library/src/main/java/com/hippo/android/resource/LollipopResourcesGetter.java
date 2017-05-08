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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.M)
class LollipopResourcesGetter implements ResourcesGetter {

  @Override
  public ColorStateList getColorStateList(Context context, int id) {
    return context.getColorStateList(id);
  }

  @Override
  public Drawable getDrawable(Context context, int id) {
    return context.getDrawable(id);
  }
}
