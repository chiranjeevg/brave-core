// Copyright (c) 2021 The Brave Authors. All rights reserved.
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this file,
// you can obtain one at http://mozilla.org/MPL/2.0/.

#include "brave/components/ntp_background_images/browser/ntp_custom_background_images_service.h"

#include "base/notreached.h"
#include "base/values.h"
#include "brave/components/ntp_background_images/browser/url_constants.h"
#include "components/prefs/pref_service.h"

namespace ntp_background_images {

NTPCustomBackgroundImagesService::NTPCustomBackgroundImagesService(
    PrefService* prefs,
    const std::string& custom_bg_pref_name)
    : prefs_(prefs), custom_bg_pref_name_(custom_bg_pref_name) {}

NTPCustomBackgroundImagesService::~NTPCustomBackgroundImagesService() = default;

bool NTPCustomBackgroundImagesService::ShouldShowCustomBackground() const {
  return prefs_->GetBoolean(custom_bg_pref_name_);
}

base::Value NTPCustomBackgroundImagesService::GetBackground() const {
  base::Value data(base::Value::Type::DICTIONARY);
  // TODO(simonhong): Set url from client.
  data.SetStringKey(kWallpaperImageURLKey,
                    "chrome://new-tab-page/background.jpg");
  return data;
}

void NTPCustomBackgroundImagesService::Shutdown() {}

}  // namespace ntp_background_images
