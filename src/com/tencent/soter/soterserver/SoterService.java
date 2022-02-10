/**
 * Copyright (c) 2018, Spreadtrum Communications Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.tencent.soter.soterserver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import vendor.sprd.hardware.soter.V1_0.ISoter;
import vendor.sprd.hardware.soter.V1_0.SoterErrorCode;



public class SoterService extends Service {

    private static final String TAG = "SoterService";

    public SoterService() {
        Log.v(TAG, "SoterService");
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "OnCreate");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "OnBind");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
    }

    public static class UserAppUninstallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "onReceive in");
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_FULLY_REMOVED)) {
                //remove askey and all authkeys
                int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                Log.i(TAG, "install: isReplacing = " + isReplacing);
                Log.i(TAG, "install: uid = " + uid);
                if (!isReplacing) {
                    Log.i(TAG, "begin to use uid to remove ask");

                    int errcode = SoterErrorCode.SOTER_WRAPPERERROR_UNKNOWN;
                    try {
                        ISoter soterService = ISoter.getService(true);

                        if (soterService != null) {
                            Log.i(TAG, "start removeAllAuthKey");
                            errcode = soterService.removeAllUidKey(uid);
                        } else {
                            Log.e(TAG, "soter hidl service gotten is null");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                }
            }
        }
    }


    private final IBinder mBinder = new ServiceStub(this);

    private final class ServiceStub extends ISoterService.Stub {
        WeakReference<SoterService> mService;

        ServiceStub(SoterService service) {
            mService = new WeakReference<SoterService>(service);
        }

        @Override
        public int generateAppSecureKey(int uid) throws RemoteException {
            Log.i(TAG,"generateAppSecureKey in");
            int errcode = SoterErrorCode.SOTER_WRAPPERERROR_UNKNOWN;

            try {
                ISoter soterService = ISoter.getService(true);
                if (soterService != null) {
                    Log.i(TAG, "start generateAppSecureKey");
                    errcode = soterService.generateAskKeyPair(uid);
                } else {
                    Log.e(TAG, "soter hidl service gotten is null");
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            Log.i(TAG, "generateAppSecureKey errcode:" + errcode);
            return errcode;

        }

        @Override
        public SoterExportResult getAppSecureKey(int uid) throws RemoteException {
            Log.i(TAG, "getAppSecureKey in");
            final SoterExportResult result = new SoterExportResult();

            result.resultCode = SoterErrorCode.SOTER_ERROR_UNKNOWN_ERROR;

            try {
                ISoter soterService = ISoter.getService(true);
                if (soterService != null) {
                    Log.i(TAG, "start exportAskPublicKey");
                    soterService.exportAskPublicKey(uid, new ISoter.exportAskPublicKeyCallback() {

                        @Override
                        public void onValues(int error, ArrayList<Byte> data, int dataLength) {
                            result.resultCode = error;
                            result.exportData = listTobyte(data);
                            result.exportDataLength = dataLength;
                        }
                    });
                } else {
                    Log.e(TAG, "soter hidl service gotten is null");
                }
            } catch (Exception e){
                result.resultCode = SoterErrorCode.SOTER_WRAPPERERROR_UNKNOWN;
                Log.e(TAG, "exception" + e.toString());
            }
            return result;
        }

        @Override
        public boolean hasAskAlready(int uid) throws RemoteException {
            Log.v(TAG, "hasAskAlready in");

            int errcode = SoterErrorCode.SOTER_ERROR_UNKNOWN_ERROR;

            try {
                ISoter soterService = ISoter.getService(true);
                if (soterService != null) {
                    Log.i(TAG, "start hasAskAlready");
                    errcode = soterService.hasAskAlready(uid);
                } else {
                    Log.e(TAG, "soter hidl service gotten is null");
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            return (errcode == SoterErrorCode.SOTER_ERROR_OK);
        }

        @Override
        public int generateAuthKey(int uid, String kname) throws RemoteException {
            Log.i(TAG, "generateAuthKey in");

            if (kname == null || kname.length() == 0) {
                return SoterErrorCode.SOTER_ERROR_KNAME_NULL;
            }

            int errcode = SoterErrorCode.SOTER_WRAPPERERROR_UNKNOWN;
            try {
                ISoter soterService = ISoter.getService(true);
                if (soterService != null) {
                    Log.i(TAG, "start generateAuthKey");
                    errcode = soterService.generateAuthKeyPair(uid, kname);
                } else {
                    Log.e(TAG, "soter hidl service gotten is null");
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            Log.i(TAG, "generateAuthKey errcode:" + errcode);
            return errcode;
        }

        @Override
        public int removeAuthKey(int uid, String kname) throws RemoteException {
            Log.i(TAG, "removeAuthKey in");

            if (kname == null || kname.length() == 0) {
                return SoterErrorCode.SOTER_ERROR_KNAME_NULL;
            }

            int errcode = SoterErrorCode.SOTER_WRAPPERERROR_UNKNOWN;

            try {
                ISoter soterService = ISoter.getService(true);

                if (soterService != null) {
                    Log.i(TAG, "start removeAuthKey");
                    errcode = soterService.removeAuthKey(uid, kname);
                } else {
                    Log.e(TAG, "soter hidl service gotten is null");
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            Log.i(TAG, "removeAuthKeyImp errcode:" + errcode);
            return errcode;
        }

        @Override
        public SoterExportResult getAuthKey(int uid, String kname) throws RemoteException {
            Log.i(TAG, "getAuthKey in");
            final SoterExportResult result = new SoterExportResult();

            result.resultCode = SoterErrorCode.SOTER_ERROR_UNKNOWN_ERROR;
            if (kname == null || kname.length() == 0) {
                result.resultCode = SoterErrorCode.SOTER_ERROR_KNAME_NULL;
                return result;
            }

            try {
                ISoter soterService = ISoter.getService(true);
                if (soterService != null) {
                    Log.i(TAG, "start exportAuthKeyPublicKey");
                    soterService.exportAuthKeyPublicKey(uid, kname, new ISoter.exportAuthKeyPublicKeyCallback() {

                        @Override
                        public void onValues(int error, ArrayList<Byte> data, int dataLength) {
                            result.resultCode = error;
                            result.exportData = listTobyte(data);
                            result.exportDataLength = dataLength;
                        }

                    });
                } else {
                    Log.e(TAG, "soter hidl service gotten is null");
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            Log.i(TAG, "exportAuthKeyPublicKey errcode:" + result.resultCode);
            return result;
        }

        @Override
        public int removeAllAuthKey(int uid) throws RemoteException {
            Log.i(TAG, "removeAllAuthKey in");

            int errcode = SoterErrorCode.SOTER_WRAPPERERROR_UNKNOWN;
            try {
                ISoter soterService = ISoter.getService(true);

                if (soterService != null) {
                    Log.i(TAG, "start removeAllAuthKey");
                    errcode = soterService.removeAllUidKey(uid);
                } else {
                    Log.e(TAG, "soter hidl service gotten is null");
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            Log.i(TAG, "removeAllAuthKey errcode:" + errcode);
            return errcode;
        }

        @Override
        public boolean hasAuthKey(int uid, String kname) throws RemoteException {
            Log.i(TAG, "hasAuthKey in");

            if (kname == null || kname.length() == 0) {
                Log.d(TAG, "kname is null");
                return false;
            }

            int errcode = SoterErrorCode.SOTER_ERROR_UNKNOWN_ERROR;

            try {
                ISoter soterService = ISoter.getService(true);

                if (soterService != null) {
                    Log.i(TAG, "start hasAuthKeyImp");
                    errcode = soterService.hasAuthKey(uid, kname);
                } else {
                    Log.e(TAG, "soter hidl service gotten is null");
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            return (errcode == SoterErrorCode.SOTER_ERROR_OK);

        }

        @Override
        public SoterSessionResult initSigh(int uid, String kname, String challenge) throws RemoteException {
            Log.i(TAG, "initSigh in");
            final SoterSessionResult result = new SoterSessionResult();

            if (kname == null || kname.length() == 0) {
                result.resultCode = SoterErrorCode.SOTER_ERROR_KNAME_NULL;
                return result;
            }

            if (challenge == null || challenge.length() == 0) {
                result.resultCode = SoterErrorCode.SOTER_ERROR_CHALLENGE_NULL;
                return result;
            }

            try {
                ISoter soterService = ISoter.getService(true);
                if (soterService != null) {
                    Log.i(TAG, "start initSign");
                    soterService.initSign(uid, kname, challenge, new ISoter.initSignCallback() {

                        @Override
                        public void onValues(int tempErrorCode, long tempSession) {
                            result.session = tempSession;
                            result.resultCode = tempErrorCode;
                        }
                    });
                } else {
                    Log.e(TAG, "soter hidl service gotten is null");
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }


            Log.i(TAG, "initSign errcode:" + result.resultCode);
            return result;
        }

        @Override
        public SoterSignResult finishSign(long signSession) throws RemoteException {
            Log.i(TAG, "finishSignImp in");
            final SoterSignResult result = new SoterSignResult();

            if (signSession == 0) {
                result.resultCode = SoterErrorCode.SOTER_ERROR_OPERATEID_NULL;
            }

            try {
                ISoter soterService = ISoter.getService(true);
                if (soterService != null) {
                    Log.i(TAG, "start initSign");
                    soterService.finishSign(signSession, new ISoter.finishSignCallback() {

                        @Override
                        public void onValues(int error, ArrayList<Byte> data, int dataLength) {
                            result.resultCode = error;
                            result.exportData = listTobyte(data);
                            result.exportDataLength = dataLength;
                        }

                    });
                } else {
                    Log.e(TAG, "soter hidl service gotten is null");
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            Log.i(TAG, "finishSignImp errcode:" + result.resultCode);
            return result;
        }

        @Override
        public SoterDeviceResult getDeviceId() throws RemoteException {
            Log.i(TAG, "getDeviceId in");
            final SoterDeviceResult result = new SoterDeviceResult();

            try {
                ISoter soterService = ISoter.getService(true);
                if (soterService != null) {
                    Log.i(TAG, "start getDeviceId");
                    soterService.getDeviceId(new ISoter.getDeviceIdCallback() {

                        @Override
                        public void onValues(int error, ArrayList<Byte> deviceId, int deviceIdLength) {
                            result.resultCode = error;
                            result.exportData = listTobyte(deviceId);
                            result.exportDataLength = deviceIdLength;
                        }

                    });
                } else {
                    Log.e(TAG, "soter hidl service gotten is null");
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            Log.i(TAG, "getDeviceId errcode:" + result.resultCode);
            return result;
        }

        @Override
        public int getVersion() throws RemoteException {
            return 1;
        }

    };


    private byte[] listTobyte(List<Byte> list) {
        if (list == null || list.size() == 0)
            return null;
        byte[] bytes = new byte[list.size()];
        int i = 0;
        for (Byte aList : list) {
            bytes[i] = aList;
            i++;
        }
        return bytes;
    }
}
