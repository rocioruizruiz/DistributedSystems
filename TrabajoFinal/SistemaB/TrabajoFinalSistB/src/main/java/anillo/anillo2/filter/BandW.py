originalImage = cv2.imread(imgpath)
global n
grayScale = cv2.cvtColor(originalImage, cv2.COLOR_BGR2GRAY)
(thresh, BandW) = cv2.threshold(grayScale, 127, 255, cv2.THRESH_BINARY)
path = "/mnt/clientFilteredImg/"
destPath = os.path.join(path, 'filtered' + str(n) + '.jpg')
cv2.imwrite(destPath, BandW)
n += 1
print destPath
