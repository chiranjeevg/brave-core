/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.chromium.chrome.browser.crypto_wallet.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.chromium.base.Log;
import org.chromium.brave_wallet.mojom.BraveWalletService;
import org.chromium.brave_wallet.mojom.ErcToken;
import org.chromium.brave_wallet.mojom.ErcTokenRegistry;
import org.chromium.brave_wallet.mojom.KeyringController;
import org.chromium.chrome.R;
import org.chromium.chrome.browser.crypto_wallet.ERCTokenRegistryFactory;
import org.chromium.chrome.browser.crypto_wallet.activities.BraveWalletActivity;
import org.chromium.chrome.browser.crypto_wallet.activities.BuySendSwapActivity;
import org.chromium.chrome.browser.crypto_wallet.adapters.WalletCoinAdapter;
import org.chromium.chrome.browser.crypto_wallet.listeners.OnWalletListItemClick;
import org.chromium.chrome.browser.crypto_wallet.model.WalletListItemModel;
import org.chromium.chrome.browser.crypto_wallet.observers.KeyringControllerObserver;
import org.chromium.chrome.browser.crypto_wallet.util.TokenUtils;
import org.chromium.chrome.browser.crypto_wallet.util.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class EditVisibleAssetsBottomSheetDialogFragment extends BottomSheetDialogFragment
        implements View.OnClickListener, OnWalletListItemClick, KeyringControllerObserver {
    public static final String TAG_FRAGMENT =
            EditVisibleAssetsBottomSheetDialogFragment.class.getName();
    private WalletCoinAdapter walletCoinAdapter;
    private WalletCoinAdapter.AdapterType mType;
    private String mChainId;
    private DismissListener mDismissListener;
    private Boolean mIsAssetsListChanged;

    public interface DismissListener {
        void onDismiss(Boolean isAssetsListChanged);
    }

    public static EditVisibleAssetsBottomSheetDialogFragment newInstance(
            WalletCoinAdapter.AdapterType type) {
        return new EditVisibleAssetsBottomSheetDialogFragment(type);
    }

    private EditVisibleAssetsBottomSheetDialogFragment(WalletCoinAdapter.AdapterType type) {
        mType = type;
    }

    private ErcTokenRegistry getErcTokenRegistry() {
        Activity activity = getActivity();
        if (activity instanceof BraveWalletActivity) {
            return ((BraveWalletActivity) activity).getErcTokenRegistry();
        } else if (activity instanceof BuySendSwapActivity) {
            return ((BuySendSwapActivity) activity).getErcTokenRegistry();
        }

        return null;
    }

    private BraveWalletService getBraveWalletService() {
        Activity activity = getActivity();
        if (activity instanceof BraveWalletActivity) {
            return ((BraveWalletActivity) activity).getBraveWalletService();
        } else if (activity instanceof BuySendSwapActivity) {
            return ((BuySendSwapActivity) activity).getBraveWalletService();
        }

        return null;
    }

    private KeyringController getKeyringController() {
        Activity activity = getActivity();
        if (activity instanceof BraveWalletActivity) {
            return ((BraveWalletActivity) activity).getKeyringController();
        } else if (activity instanceof BuySendSwapActivity) {
            return ((BuySendSwapActivity) activity).getKeyringController();
        }

        return null;
    }

    public void setChainId(String chainId) {
        assert (chainId != null && !chainId.isEmpty());
        mChainId = chainId;
    }

    public void setDismissListener(DismissListener dismissListener) {
        mDismissListener = dismissListener;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            EditVisibleAssetsBottomSheetDialogFragment fragment =
                    (EditVisibleAssetsBottomSheetDialogFragment) manager.findFragmentByTag(
                            EditVisibleAssetsBottomSheetDialogFragment.TAG_FRAGMENT);
            FragmentTransaction transaction = manager.beginTransaction();
            if (fragment != null) {
                transaction.remove(fragment);
            }
            transaction.add(this, tag);
            transaction.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.e("EditVisibleAssetsBottomSheetDialogFragment", e.getMessage());
        }
    }

    @Override
    public void locked() {
        dismiss();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                setupFullHeight(bottomSheetDialog);
            }
        });
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mDismissListener != null) {
            mDismissListener.onDismiss(mIsAssetsListChanged);
        }
    }

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet =
                (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        int windowHeight = getWindowHeight();
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        @SuppressLint("InflateParams")
        final View view = LayoutInflater.from(getContext())
                                  .inflate(R.layout.edit_visible_assets_bottom_sheet, null);

        dialog.setContentView(view);
        ViewParent parent = view.getParent();
        ((View) parent).getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        Button saveAssets = view.findViewById(R.id.saveAssets);
        TextView addCustomAsset = view.findViewById(R.id.add_custom_asset);
        if (mType == WalletCoinAdapter.AdapterType.EDIT_VISIBLE_ASSETS_LIST) {
            saveAssets.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View clickView) {
                    dismiss();
                }
            });
            addCustomAsset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View clickView) {
                    Dialog dialog = new Dialog(getActivity());
                    dialog.setContentView(R.layout.brave_wallet_add_custom_asset);
                    dialog.show();

                    Button cancel = dialog.findViewById(R.id.cancel);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    Button add = dialog.findViewById(R.id.add);
                    filterAddCustomAssetTextWatcher.setDialog(dialog);
                    EditText tokenNameEdit = dialog.findViewById(R.id.token_name);
                    EditText tokenContractAddressEdit =
                            dialog.findViewById(R.id.token_contract_address);
                    EditText tokenSymbolEdit = dialog.findViewById(R.id.token_symbol);
                    EditText tokenDecimalsEdit = dialog.findViewById(R.id.token_decimals);
                    tokenNameEdit.addTextChangedListener(filterAddCustomAssetTextWatcher);
                    tokenContractAddressEdit.addTextChangedListener(
                            filterAddCustomAssetTextWatcher);
                    tokenSymbolEdit.addTextChangedListener(filterAddCustomAssetTextWatcher);
                    tokenDecimalsEdit.addTextChangedListener(filterAddCustomAssetTextWatcher);
                    add.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            BraveWalletService braveWalletService = getBraveWalletService();
                            assert braveWalletService != null;
                            ErcToken token = new ErcToken();
                            token.contractAddress = tokenContractAddressEdit.getText().toString();
                            token.name = tokenNameEdit.getText().toString();
                            token.logo = "";
                            token.isErc20 = true;
                            // We support Erc20 tokens only for now
                            token.isErc721 = false;
                            token.symbol = tokenSymbolEdit.getText().toString();
                            token.decimals = 18;
                            try {
                                token.decimals =
                                        Integer.valueOf(tokenDecimalsEdit.getText().toString());
                            } catch (NumberFormatException exc) {
                            }
                            token.visible = true;
                            braveWalletService.addUserAsset(token, mChainId, success -> {
                                if (success) {
                                    WalletListItemModel itemModel = new WalletListItemModel(
                                            R.drawable.ic_eth, token.name, token.symbol, "", "");
                                    itemModel.setErcToken(token);
                                    itemModel.setIconPath(token.logo);

                                    itemModel.setIsUserSelected(true);
                                    walletCoinAdapter.addItem(itemModel);
                                    mIsAssetsListChanged = true;
                                }
                                dialog.dismiss();
                            });
                        }
                    });
                }
            });
        } else {
            saveAssets.setVisibility(View.GONE);
            addCustomAsset.setVisibility(View.GONE);
        }

        ErcTokenRegistry ercTokenRegistry = getErcTokenRegistry();
        if (ercTokenRegistry != null) {
            BraveWalletService braveWalletService = getBraveWalletService();
            assert braveWalletService != null;
            if (mType == WalletCoinAdapter.AdapterType.EDIT_VISIBLE_ASSETS_LIST) {
                assert mChainId != null && !mChainId.isEmpty();
                TokenUtils.getUserAssetsFiltered(braveWalletService, mChainId, (userAssets) -> {
                    TokenUtils.getAllTokensFiltered(
                            braveWalletService, ercTokenRegistry, mChainId, tokens -> {
                                tokens = Utils.fixupTokensRegistry(tokens, mChainId);
                                setUpAssetsList(view, tokens, userAssets);
                            });
                });
            } else if (mType == WalletCoinAdapter.AdapterType.SEND_ASSETS_LIST) {
                assert mChainId != null && !mChainId.isEmpty();
                TokenUtils.getUserAssetsFiltered(braveWalletService, mChainId,
                        tokens -> { setUpAssetsList(view, tokens, new ErcToken[0]); });
            } else if (mType == WalletCoinAdapter.AdapterType.SWAP_TO_ASSETS_LIST
                    || mType == WalletCoinAdapter.AdapterType.SWAP_FROM_ASSETS_LIST) {
                assert mChainId != null && !mChainId.isEmpty();
                TokenUtils.getAllTokensFiltered(braveWalletService, ercTokenRegistry, mChainId,
                        tokens -> { setUpAssetsList(view, tokens, new ErcToken[0]); });
            } else if (mType == WalletCoinAdapter.AdapterType.BUY_ASSETS_LIST) {
                TokenUtils.getBuyTokensFiltered(ercTokenRegistry,
                        tokens -> { setUpAssetsList(view, tokens, new ErcToken[0]); });
            }
        }
        KeyringController keyringController = getKeyringController();
        assert keyringController != null;
        keyringController.addObserver(this);
    }

    private TextWatcherImpl filterAddCustomAssetTextWatcher = new TextWatcherImpl();

    private class TextWatcherImpl implements TextWatcher {
        Dialog mDialog;

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            EditText tokenNameEdit = mDialog.findViewById(R.id.token_name);
            EditText tokenContractAddressEdit = mDialog.findViewById(R.id.token_contract_address);
            EditText tokenSymbolEdit = mDialog.findViewById(R.id.token_symbol);
            EditText tokenDecimalsEdit = mDialog.findViewById(R.id.token_decimals);
            Button addButton = mDialog.findViewById(R.id.add);
            if (tokenNameEdit.getText().toString().isEmpty()
                    || !tokenContractAddressEdit.getText().toString().startsWith("0x")
                    || tokenSymbolEdit.getText().toString().isEmpty()
                    || tokenDecimalsEdit.getText().toString().isEmpty()) {
                addButton.setEnabled(false);

                return;
            }

            addButton.setEnabled(true);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private void setUpAssetsList(View view, ErcToken[] tokens, ErcToken[] userSelectedTokens) {
        HashSet<String> selectedTokensSymbols = new HashSet<String>();
        for (ErcToken userSelectedToken : userSelectedTokens) {
            selectedTokensSymbols.add(userSelectedToken.symbol.toUpperCase(Locale.getDefault()));
        }
        RecyclerView rvAssets = view.findViewById(R.id.rvAssets);
        walletCoinAdapter = new WalletCoinAdapter(mType);
        List<WalletListItemModel> walletListItemModelList = new ArrayList<>();
        String tokensPath = ERCTokenRegistryFactory.getInstance().getTokensIconsLocation();
        // Add ETH as a first item always
        ErcToken eth = Utils.createEthereumErcToken();
        WalletListItemModel itemModelEth =
                new WalletListItemModel(R.drawable.ic_eth, eth.name, eth.symbol, "", "");
        itemModelEth.setIsUserSelected(
                selectedTokensSymbols.contains(eth.symbol.toUpperCase(Locale.getDefault())));
        itemModelEth.setIconPath("file://" + tokensPath + "/" + eth.logo);
        itemModelEth.setErcToken(eth);
        walletListItemModelList.add(itemModelEth);
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].symbol.equals("ETH")) {
                // We have added ETH already
                continue;
            }
            WalletListItemModel itemModel = new WalletListItemModel(
                    R.drawable.ic_eth, tokens[i].name, tokens[i].symbol, "", "");
            itemModel.setErcToken(tokens[i]);
            itemModel.setIconPath("file://" + tokensPath + "/" + tokens[i].logo);

            boolean isUserSelected = selectedTokensSymbols.contains(
                    tokens[i].symbol.toUpperCase(Locale.getDefault()));
            itemModel.setIsUserSelected(isUserSelected);
            walletListItemModelList.add(itemModel);
        }
        walletCoinAdapter.setWalletListItemModelList(walletListItemModelList);
        walletCoinAdapter.setOnWalletListItemClick(this);
        walletCoinAdapter.setWalletListItemType(Utils.ASSET_ITEM);
        rvAssets.setAdapter(walletCoinAdapter);
        rvAssets.setLayoutManager(new LinearLayoutManager(getActivity()));
        SearchView searchView = (SearchView) view.findViewById(R.id.searchView);
        searchView.setQueryHint(getText(R.string.search_tokens));
        searchView.setIconified(false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (walletCoinAdapter != null) {
                    walletCoinAdapter.filter(query);
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (walletCoinAdapter != null) {
                    walletCoinAdapter.filter(newText);
                }

                return true;
            }
        });
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }

    @Override
    public void onAssetClick(ErcToken token) {
        List<WalletListItemModel> checkedAssets = walletCoinAdapter.getCheckedAssets();
        Activity activity = getActivity();
        if (activity instanceof BuySendSwapActivity && checkedAssets.size() > 0) {
            if (mType == WalletCoinAdapter.AdapterType.SEND_ASSETS_LIST
                    || mType == WalletCoinAdapter.AdapterType.BUY_ASSETS_LIST
                    || mType == WalletCoinAdapter.AdapterType.SWAP_FROM_ASSETS_LIST) {
                ((BuySendSwapActivity) activity)
                        .updateBuySendAsset(checkedAssets.get(0).getSubTitle(),
                                checkedAssets.get(0).getErcToken());
            } else if (mType == WalletCoinAdapter.AdapterType.SWAP_TO_ASSETS_LIST) {
                ((BuySendSwapActivity) activity)
                        .updateSwapToAsset(checkedAssets.get(0).getSubTitle(),
                                checkedAssets.get(0).getErcToken());
            }
        }
        dismiss();
    }

    @Override
    public void onTrashIconClick(WalletListItemModel walletListItemModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.wallet_remove_custom_asset);
        builder.setMessage(String.format(getString(R.string.wallet_remove_custom_asset_desc),
                walletListItemModel.getErcToken().name, walletListItemModel.getErcToken().symbol));

        builder.setPositiveButton(
                R.string.wallet_remove_custom_asset_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        BraveWalletService braveWalletService = getBraveWalletService();
                        assert braveWalletService != null;
                        assert (mChainId != null && !mChainId.isEmpty());
                        braveWalletService.removeUserAsset(
                                walletListItemModel.getErcToken(), mChainId, (success) -> {
                                    if (success) {
                                        walletCoinAdapter.removeItem(walletListItemModel);
                                        mIsAssetsListChanged = true;
                                    }
                                });
                    }
                });
        builder.setNegativeButton(R.string.wallet_remove_custom_asset_no, null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onAssetCheckedChanged(WalletListItemModel walletListItemModel, boolean isChecked) {
        if (mType == WalletCoinAdapter.AdapterType.EDIT_VISIBLE_ASSETS_LIST) {
            TokenUtils.isCustomToken(
                    walletListItemModel.getErcToken(), getErcTokenRegistry(), isCustom -> {
                        BraveWalletService braveWalletService = getBraveWalletService();
                        assert braveWalletService != null;
                        assert (mChainId != null && !mChainId.isEmpty());
                        if (!isCustom) {
                            if (isChecked) {
                                braveWalletService.addUserAsset(
                                        walletListItemModel.getErcToken(), mChainId, (success) -> {
                                            if (success) {
                                                walletListItemModel.setIsUserSelected(true);
                                            }
                                        });
                            } else {
                                braveWalletService.removeUserAsset(
                                        walletListItemModel.getErcToken(), mChainId, (success) -> {
                                            if (success) {
                                                walletListItemModel.setIsUserSelected(false);
                                            }
                                        });
                            }
                        } else {
                            braveWalletService.setUserAssetVisible(
                                    walletListItemModel.getErcToken(), mChainId, isChecked,
                                    success -> {
                                        if (success) {
                                            walletListItemModel.setIsUserSelected(isChecked);
                                        }
                                    });
                        }
                        mIsAssetsListChanged = true;
                    });
        }
    };
}
