
## 基本说明

NFC读卡，zxing扫码库

## 依赖

### 基于android support 26.0.2编译

    implementation 'com.github.kellysong.my-hardware:nfcard:1.0.0'
    implementation 'com.github.kellysong.my-hardware:zxing:1.0.0'

### 基于androidx(appcompat 1.1.0)编译

    implementation 'com.github.kellysong.my-hardware:nfcard:1.1.0'
    implementation 'com.github.kellysong.my-hardware:zxing:1.1.0'


### 基于androidx(appcompat 1.2.0)编译

     implementation 'com.github.kellysong.my-hardware:nfcard:1.1.2'
     implementation 'com.github.kellysong.my-hardware:zxing:1.1.2'

最新版本：

    implementation 'com.github.kellysong.my-hardware:nfcard:1.1.3'
    implementation 'com.github.kellysong.my-hardware:zxing:1.1.3'

## 使用
	
NFC读卡：

    <activity
            android:name="com.sinpo.xnfc.NFCardActivity"
            android:configChanges="keyboardHidden|orientation"
            android:launchMode="singleTask"
            android:screenOrientation="portrait"
            android:windowBackground="@null"
            android:windowSoftInputMode="adjustUnspecified|stateAlwaysHidden">
            <intent-filter>
                <action android:name="android.nfc.action.TECH_DISCOVERED" />
            </intent-filter>

            <meta-data
                android:name="android.nfc.action.TECH_DISCOVERED"
                android:resource="@xml/nfc_tech_filter" />

            <intent-filter>
                <action android:name="android.nfc.action.TAG_DISCOVERED" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>

zxing扫码:

         <activity
           android:name="com.renny.zxing.Activity.CaptureActivity"
           android:screenOrientation="portrait">
       </activity>