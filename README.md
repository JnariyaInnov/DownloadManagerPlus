## DownloadManagerPlus

Using faster and easier than Android Download Manager

## Screenshots

![Screenshot](https://gifyu.com/images/downloadmanagerplus-v1.1.1.gif)

## Getting started

##### Dependency

    dependencies {
        compile 'com.siaray:downloadmanagerplus:1.1.2'
    }

## Usage

##### To start the download.

    Downloader downloader = Downloader.getInstance(context, downloadManager)
     .setUrl(url)
     .setListener(listener)
     .setId(id)
     .setAllowedOverRoaming(roamingAllowed)
     .setAllowedOverMetered(meteredAllowed) //Api 16 and higher
     .setVisibleInDownloadsUi(isVisible)
     .setDestinationDir(path, fileName)
     .setNotificationTitle(notificationTitle)
     .setDescription(description)
     .setNotificationVisibility(visibility)
     .setAllowedNetworkTypes(networkTypes)
     .start();

##### To view download status and progress that has already started.

    downloader.showProgress();

##### To cancel a download.
> `id parameter is download plus id`

    downloader.cancel(id);

##### Detect download status.

    downloader.getStatus(id);

##### Delete the downloaded file.

    downloader.deleteFile(id, deleteListener);

##### Get download id.

    Downloader.getDownloadId(context, id);

##### Get download plus id.

    Downloader.getId(context, downloadId);

##### Get download item.

    Downloader.getDownloadItem(context, downloadManager, id);

##### Get downloads list.

    Downloader.getDownloadsList(context, downloadManager);

## Licence

    Copyright 2017 Siamak Rayeji

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.