def BandW(filtro, imgPath):
    originalImage = cv2.imread(imgPath)
    global n
    grayScale = cv2.cvtColor(originalImage, cv2.COLOR_BGR2GRAY)
    (thresh, BandW) = cv2.threshold(grayScale, 127, 255, cv2.THRESH_BINARY)
    path = os.path.split(imgPath)[0]
    destPath = os.path.join(path, 'filtered' + str(n) + '.jpg')
    cv2.imwrite(destPath, BandW)
    n += 1
    return destPath
