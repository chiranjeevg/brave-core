// Copyright (c) 2021 The Brave Authors. All rights reserved.
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this file,
// you can obtain one at http://mozilla.org/MPL/2.0/.

#ifndef BRAVE_BROWSER_UI_VIEWS_BRAVE_ACTIONS_BRAVE_SHIELDS_ACTION_VIEW_H_
#define BRAVE_BROWSER_UI_VIEWS_BRAVE_ACTIONS_BRAVE_SHIELDS_ACTION_VIEW_H_

#include <cstddef>
#include <memory>

#include "brave/browser/ui/brave_actions/brave_action_icon_with_badge_image_source.h"  // NOLINT
#include "brave/browser/ui/brave_shields_data_controller.h"
#include "chrome/browser/ui/tabs/tab_strip_model_observer.h"
#include "ui/views/controls/button/label_button.h"

class BraveShieldsActionView
    : public views::LabelButton,
      public brave_shields::BraveShieldsDataController::Observer,
      public TabStripModelObserver {
 public:
  explicit BraveShieldsActionView(TabStripModel* tab_strip_model);
  BraveShieldsActionView(const BraveShieldsActionView&) = delete;
  BraveShieldsActionView& operator=(const BraveShieldsActionView&) = delete;
  ~BraveShieldsActionView() override;

  void Init();
  void Update();
  // views::LabelButton:
  std::unique_ptr<views::LabelButtonBorder> CreateDefaultBorder()
      const override;
  // brave_shields::BraveShieldsDataController
  void OnResourcesCountChange(const int count) override;
  // TabStripModelObserver
  void OnTabStripModelChanged(
      TabStripModel* tab_strip_model,
      const TabStripModelChange& change,
      const TabStripSelectionChange& selection) override;
  SkPath GetHighlightPath() const;

 private:
  void ButtonPressed();
  void UpdateIconState();
  gfx::ImageSkia GetIconImage(bool is_enabled);
  std::unique_ptr<IconWithBadgeImageSource> GetImageSource();

  TabStripModel* tab_strip_model_ = nullptr;
};

#endif  // BRAVE_BROWSER_UI_VIEWS_BRAVE_ACTIONS_BRAVE_SHIELDS_ACTION_VIEW_H_
